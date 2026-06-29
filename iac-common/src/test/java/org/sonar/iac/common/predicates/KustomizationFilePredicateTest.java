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
package org.sonar.iac.common.predicates;

import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.predicates.FilePredicateTestUtils.newInputFileMock;

class KustomizationFilePredicateTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @ParameterizedTest
  @MethodSource
  void shouldDetectKustomizationFile(String path, boolean expectedMatch) throws IOException {
    var predicate = new KustomizationFilePredicate(false);
    assertThat(predicate.accept(newInputFileMock(path, ""))).isEqualTo(expectedMatch);
  }

  static Stream<Arguments> shouldDetectKustomizationFile() {
    return Stream.of(
      Arguments.of("kustomization.yaml", true),
      Arguments.of("kustomization.yml", true),
      Arguments.of("base/overlays/kustomization.yaml", true),
      // file names are matched case-insensitively
      Arguments.of("KUSTOMIZATION.YAML", true),
      Arguments.of("Kustomization.Yml", true),
      // only the exact file names are matched
      Arguments.of("deployment.yaml", false),
      Arguments.of("kustomization.json", false),
      Arguments.of("my-kustomization.yaml", false),
      Arguments.of("kustomization.yaml.bak", false));
  }

  @Test
  void shouldReturnKustomizeFileType() {
    assertThat(new KustomizationFilePredicate(false).fileType()).isEqualTo(FileType.KUSTOMIZE);
  }

  @Test
  void shouldLogWhenDebugLoggingEnabled() throws IOException {
    var predicate = new KustomizationFilePredicate(true);
    assertThat(predicate.accept(newInputFileMock("kustomization.yaml", ""))).isTrue();
    assertThat(logTester.logs(Level.DEBUG)).anyMatch(log -> log.contains("Identified as Kustomization file"));
  }

  @Test
  void shouldNotLogWhenDebugLoggingDisabled() throws IOException {
    var predicate = new KustomizationFilePredicate(false);
    assertThat(predicate.accept(newInputFileMock("kustomization.yaml", ""))).isTrue();
    assertThat(logTester.logs(Level.DEBUG)).noneMatch(log -> log.contains("Identified as Kustomization file"));
  }
}
