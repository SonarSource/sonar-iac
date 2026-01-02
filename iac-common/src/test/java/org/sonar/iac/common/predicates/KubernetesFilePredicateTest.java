/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class KubernetesFilePredicateTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  @TempDir
  Path tempDir;

  @ParameterizedTest
  @MethodSource
  void shouldDetectKubernetesFile(String content, boolean shouldMatch) {
    var predicate = new KubernetesFilePredicate(true);
    assertThat(predicate.apply(IacTestUtils.inputFile("test.yaml", tempDir, content, "kubernetes"))).isEqualTo(shouldMatch);

    if (shouldMatch) {
      assertThat(logTester.logs(Level.DEBUG)).isEmpty();
    } else {
      assertThat(logTester.logs(Level.DEBUG)).contains("File without Kubernetes identifier: test.yaml");
    }
  }

  static Stream<Arguments> shouldDetectKubernetesFile() {
    return Stream.of(
      of("apiVersion: v1", false),
      of("kind: Pod", false),
      of("""
        metadata:
          labels:
            foo: bar
        """, false),
      of("""
        apiVersion: v1
        kind: Pod
        """, false),
      of("""
        apiVersion: v1
        kind: Pod
        metadata:
          labels:
            foo: bar
        """, true),
      of("""
        apiVersion: v1
        kind: Pod
        ---
        metadata:
          labels:
            foo: bar
        """, false));
  }

  @Test
  void shouldNotLogWhenDebugDisabled() {
    var predicateNoLog = new KubernetesFilePredicate(false);
    assertThat(predicateNoLog.apply(IacTestUtils.inputFile("test.yaml", tempDir, "apiVersion: v1", "kubernetes"))).isFalse();
    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }
}
