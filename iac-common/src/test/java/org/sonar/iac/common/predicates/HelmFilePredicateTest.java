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
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class HelmFilePredicateTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  @TempDir
  private Path tempDir;
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

  private HelmFilePredicate newPredicate() {
    return newPredicate(false);
  }

  private HelmFilePredicate newPredicate(boolean enablePredicateDebugLogs) {
    var predicate = new HelmFilePredicate(context.fileSystem(), enablePredicateDebugLogs);
    predicate.applyTimers(durationStatistics);
    return predicate;
  }

  @ParameterizedTest
  @ValueSource(strings = {"templates/my-pod.yaml", "templates/nested/my-pod.yaml", "values.yaml", "values.yml"})
  void shouldMatchHelmYamlFile(String path) throws IOException {
    var predicate = newPredicate();
    Files.createFile(tempDir.resolve("Chart.yaml"));

    var matches = predicate.apply(IacTestUtils.inputFile(path, tempDir, POD_SPEC, "yaml"));

    assertThat(matches).isTrue();
  }

  @Test
  void shouldMatchHelmTplFile() throws IOException {
    var predicate = newPredicate();
    Files.createFile(tempDir.resolve("Chart.yaml"));

    var matches = predicate.apply(IacTestUtils.inputFile("templates/utils.tpl", tempDir, POD_SPEC, ""));

    assertThat(matches).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"yaml", "kubernetes"})
  void shouldNotMatchKubernetesFileOutsideHelmProject(String language) {
    var predicate = newPredicate();

    var matches = predicate.apply(IacTestUtils.inputFile("test.yaml", tempDir, POD_SPEC, language));

    assertThat(matches).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"templates/my-pod.yaml", "values.yaml"})
  void shouldNotMatchHelmYamlFileWhenNotInHelmProject(String path) {
    var predicate = newPredicate();

    var matches = predicate.apply(IacTestUtils.inputFile(path, tempDir, POD_SPEC, "yaml"));

    assertThat(matches).isFalse();
  }

  @Test
  void shouldNotMatchHelmTplFileWhenNotInHelmProject() {
    var predicate = newPredicate();

    var matches = predicate.apply(IacTestUtils.inputFile("templates/utils.tpl", tempDir, POD_SPEC, ""));

    assertThat(matches).isFalse();
  }

  @Test
  void shouldNotMatchNonYamlFileInHelmProject() throws IOException {
    var predicate = newPredicate();
    Files.createFile(tempDir.resolve("Chart.yaml"));

    var matches = predicate.apply(IacTestUtils.inputFile("templates/notes.txt", tempDir, POD_SPEC, "text"));

    assertThat(matches).isFalse();
  }

  @Test
  void shouldNotMatchNonTplNonYamlFileInHelmProject() throws IOException {
    var predicate = newPredicate();
    Files.createFile(tempDir.resolve("Chart.yaml"));

    var matches = predicate.apply(IacTestUtils.inputFile("templates/utils.tp", tempDir, POD_SPEC, ""));

    assertThat(matches).isFalse();
  }

  @Test
  void shouldLogWhenIdentifiedAndDebugEnabled() throws IOException {
    var predicate = newPredicate(true);
    Files.createFile(tempDir.resolve("Chart.yaml"));
    var inputFile = IacTestUtils.inputFile("templates/my-pod.yaml", tempDir, POD_SPEC, "yaml");

    assertThat(predicate.apply(inputFile)).isTrue();
    assertThat(logTester.logs(Level.DEBUG)).contains("Identified as Helm file: " + inputFile);
  }

  @Test
  void shouldNotLogWhenDebugDisabled() throws IOException {
    var predicate = newPredicate(false);
    Files.createFile(tempDir.resolve("Chart.yaml"));

    assertThat(predicate.apply(IacTestUtils.inputFile("templates/my-pod.yaml", tempDir, POD_SPEC, "yaml"))).isTrue();
    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }

  @Test
  void shouldNotLogWhenNotIdentifiedAndDebugEnabled() {
    var predicate = newPredicate(true);

    assertThat(predicate.apply(IacTestUtils.inputFile("test.yaml", tempDir, POD_SPEC, "yaml"))).isFalse();
    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }
}
