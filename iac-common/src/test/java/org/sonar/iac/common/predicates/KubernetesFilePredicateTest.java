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

import com.sonarsource.scanner.engine.sensor.test.fixtures.SensorContextTester;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.mock;

class KubernetesFilePredicateTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  @TempDir
  Path tempDir;
  private SensorContext context;
  private final DurationStatistics durationStatistics = new DurationStatistics(mock(Configuration.class));
  private static final String POD_SPEC = """
    apiVersion: v1
    kind: Pod
    metadata:
      name: my-pod
    """;

  @BeforeEach
  void setUp() {
    context = SensorContextTester.create(tempDir);
  }

  private KubernetesFilePredicate newPredicate(boolean enablePredicateDebugLogs) {
    var predicate = new KubernetesFilePredicate(context.fileSystem(), enablePredicateDebugLogs);
    predicate.applyTimers(durationStatistics);
    return predicate;
  }

  @ParameterizedTest
  @MethodSource
  void shouldDetectKubernetesFile(String content, boolean shouldMatch) {
    var predicate = newPredicate(true);
    assertThat(predicate.apply(IacTestUtils.inputFile("test.yaml", tempDir, content, "kubernetes"))).isEqualTo(shouldMatch);

    if (shouldMatch) {
      assertThat(logTester.logs(Level.DEBUG)).isEmpty();
    } else {
      assertThat(logTester.logs(Level.DEBUG)).contains("File without Kubernetes identifier: test.yaml");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"yaml", "kubernetes"})
  void shouldMatchKubernetesFileWithLanguage(String language) {
    var predicate = newPredicate(true);

    var matches = predicate.apply(IacTestUtils.inputFile("test.yaml", tempDir, POD_SPEC, language));

    assertThat(matches).isTrue();
  }

  @Test
  void shouldNotMatchNonYamlFileWithKubernetesContent() {
    var predicate = newPredicate(true);

    var matches = predicate.apply(IacTestUtils.inputFile("test.txt", tempDir, POD_SPEC, "text"));

    assertThat(matches).isFalse();
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
    var predicateNoLog = newPredicate(false);
    assertThat(predicateNoLog.apply(IacTestUtils.inputFile("test.yaml", tempDir, "apiVersion: v1", "kubernetes"))).isFalse();
    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }
}
