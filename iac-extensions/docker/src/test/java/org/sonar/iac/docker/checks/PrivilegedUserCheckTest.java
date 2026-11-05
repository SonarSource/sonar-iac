/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.checks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.sonar.iac.common.testing.TemplateFileReader.BASE_DIR;

class PrivilegedUserCheckTest {

  private final PrivilegedUserCheck check = new PrivilegedUserCheck();

  @ParameterizedTest
  @MethodSource
  void testNonCompliant(String testFile) {
    DockerVerifier.verify(testFile, check);
  }

  static List<String> testNonCompliant() {
    return provideTestFiles("PrivilegedUserCheck/Noncompliant");
  }

  @ParameterizedTest
  @MethodSource
  void testCompliant(String testFile) {
    DockerVerifier.verifyNoIssue(testFile, check);
  }

  static List<String> testCompliant() {
    return provideTestFiles("PrivilegedUserCheck/Compliant");
  }

  @ParameterizedTest
  @MethodSource
  void testCustomSafeList(String testFile) {
    check.safeImages = "custom_image1, custom_image2, golang";
    DockerVerifier.verify(testFile, check);
  }

  static List<String> testCustomSafeList() {
    return provideTestFiles("PrivilegedUserCheck/CustomSafeImages/Noncompliant");
  }

  @ParameterizedTest
  @MethodSource
  void testCustomSafeListCompliant(String testFile) {
    check.safeImages = "custom_image1, custom_image2, golang, customHost:8080/custom/dotnet/aspnet/8.0";
    DockerVerifier.verifyNoIssue(testFile, check);
  }

  static List<String> testCustomSafeListCompliant() {
    return provideTestFiles("PrivilegedUserCheck/CustomSafeImages/Compliant");
  }

  @Test
  void testMultiStageBuild() {
    DockerVerifier.verify("PrivilegedUserCheck/Dockerfile_multi_stage_build", check);
  }

  @Test
  void testMultiStageBuildCompliant() {
    DockerVerifier.verifyNoIssue("PrivilegedUserCheck/Dockerfile_multi_stage_build-Compliant", check);
  }

  @Test
  void testMultiStageSignalConfinedToBuilderStage() {
    DockerVerifier.verify("PrivilegedUserCheck/Dockerfile_multi_stage_signal_in_builder_only", check);
  }

  @Test
  void testDockerHardenedImageCompliant() {
    DockerVerifier.verifyNoIssue("PrivilegedUserCheck/Dockerfile.compliant_docker_hardened_image", check);
  }

  @Test
  void testMirroredDockerHardenedImageStillRaises() {
    DockerVerifier.verify("PrivilegedUserCheck/Dockerfile.noncompliant_docker_hardened_image", check);
  }

  @Test
  void testChainguardImageCompliant() {
    DockerVerifier.verifyNoIssue("PrivilegedUserCheck/Dockerfile.compliant_chainguard_image", check);
  }

  @ParameterizedTest
  @MethodSource
  void testChainguardUnsafeImagesStillRaise(String testFile) {
    DockerVerifier.verify(testFile, check);
  }

  static List<String> testChainguardUnsafeImagesStillRaise() {
    return provideTestFiles("PrivilegedUserCheck/ChainguardUnsafeImages");
  }

  @Test
  void testOtherChainguardNamespaceStillRaises() {
    DockerVerifier.verify("PrivilegedUserCheck/Dockerfile.noncompliant_chainguard_image", check);
  }

  @ParameterizedTest
  @ValueSource(strings = {"exec gosu", "exec su-exec", "command gosu", "command su-exec", "exec /usr/local/bin/gosu", "exec /sbin/su-exec"})
  void shouldRecognizePrivilegeDropInHeredoc(String command) {
    DockerVerifier.verifyContentNoIssue("""
      FROM debian
      RUN cat > /entrypoint.sh <<'EOF' && chmod +x /entrypoint.sh
      #!/bin/bash
      if [ "$(id -u)" = "0" ]; then
        %s appuser:appgroup /usr/bin/myservice "$@"
      fi
      exec /usr/bin/myservice "$@"
      EOF
      ENTRYPOINT ["/entrypoint.sh"]
      """.formatted(command), check);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "exec gosu", "exec su-exec", "exec useradd", "exec adduser", "exec setpriv",
    "command gosu", "command su-exec", "command useradd", "command adduser", "command setpriv"
  })
  void shouldRecognizeSinglePrivilegeDropPrefix(String command) {
    DockerVerifier.verifyContentNoIssue("""
      FROM debian
      RUN %s appuser
      """.formatted(command), check);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "command -v gosu", "command -V su-exec", "exec -a name gosu", "command -p gosu",
    "exec command gosu", "command exec gosu", "exec exec gosu", "command command gosu",
    "echo exec gosu", "echo 'command su-exec'", "exec echo gosu", "command echo su-exec",
    "echo then gosu", "docker gosu", "then then gosu", "./gosu", "/usr/bin/echo gosu", "/opt/gosu-data/run",
    "gosu-helper", "exec su-exec-helper", "/usr/bin/gosu-helper", "command /usr/bin/su-exec-helper"
  })
  void shouldNotRecognizeUnsupportedPrefixesOrMentions(String command) {
    DockerVerifier.verifyContent("""
      FROM debian
      # Noncompliant@-1
      RUN %s appuser
      """.formatted(command), check);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "if [ \"$(id -u)\" = 0 ]; then exec gosu appuser service; fi",
    "if id appuser; then :; else adduser appuser; fi",
    "for name in appuser; do useradd \"$name\"; done",
    "{ exec su-exec appuser service; }",
    "(gosu appuser service)",
    "( setpriv --reuid=1000 service )"
  })
  void shouldRecognizePrivilegeDropAfterShellKeywordOrGrouping(String command) {
    DockerVerifier.verifyContentNoIssue("""
      FROM debian
      RUN %s
      """.formatted(command), check);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/usr/local/bin/gosu appuser", "/sbin/su-exec appuser", "/usr/sbin/useradd appuser", "/usr/sbin/adduser appuser",
    "/usr/bin/setpriv --reuid=1000 service", "/gosu appuser", "exec /usr/local/bin/gosu appuser", "command /usr/sbin/useradd appuser"
  })
  void shouldRecognizePrivilegeDropByAbsolutePath(String command) {
    DockerVerifier.verifyContentNoIssue("""
      FROM debian
      RUN %s
      """.formatted(command), check);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/usr/local/bin/gosu", "/sbin/su-exec"})
  void shouldRecognizePrivilegeDropByAbsolutePathInExecForm(String command) {
    DockerVerifier.verifyContentNoIssue("""
      FROM debian
      RUN ["%s", "appuser", "service"]
      """.formatted(command), check);
  }

  @Test
  void shouldReportExplicitRootUserRegardlessOfPrivilegeDrop() {
    DockerVerifier.verifyContent("""
      FROM debian
      RUN exec gosu appuser service
      USER root
      # Noncompliant@-1
      """, check);
  }

  private static List<String> provideTestFiles(String testFileDir) {
    try (Stream<Path> pathStream = Files.list(BASE_DIR.resolve(testFileDir))) {
      return pathStream
        .map(Path::getFileName)
        .map(fileName -> testFileDir + "/" + fileName)
        .collect(Collectors.toList());
    } catch (IOException e) {
      throw new AssertionError("Can not load test files from " + testFileDir);
    }
  }
}
