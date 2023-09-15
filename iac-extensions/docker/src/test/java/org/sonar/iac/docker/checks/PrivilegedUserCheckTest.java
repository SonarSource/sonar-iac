/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
    check.safeImages = "custom_image1, custom_image2, golang";
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
