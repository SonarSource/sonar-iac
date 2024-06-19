/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.kubernetes.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.predicates.DefaultFilePredicates;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class HelmParserTest {
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private final InputFile inputFile = mock(InputFile.class);

  private final SensorContext sensorContext = mock(SensorContext.class);
  private final HelmInputFileContext inputFileContext = spy(new HelmInputFileContext(sensorContext, inputFile));
  private final FileSystem fileSystem = mock(FileSystem.class);

  @BeforeEach
  void setup() throws URISyntaxException {
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    when(fileSystem.predicates()).thenReturn(new DefaultFilePredicates(Path.of(".")));
    when(fileSystem.baseDir()).thenReturn(new File("chart/"));
    when(inputFile.filename()).thenReturn("foo.yaml");
    when(inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
    when(inputFile.toString()).thenReturn("/chart/templates/foo.yaml");
  }

  @Test
  void shouldShiftMarkedYamlExceptions() {
    var evaluated = """
      key: | #1
        .
        .
        .
        . #2
      invalid-key #3""";

    assertThatThrownBy(() -> parseTemplate(evaluated))
      .isInstanceOf(ShiftedMarkedYamlEngineException.class);

    assertThat(logTester.logs(Level.DEBUG))
      .contains("Shifting YAML exception from [6:12] to [3:1]");
  }

  @Test
  void shouldRemoveEmptyLinesAfterEvaluation() throws IOException {
    // language=yaml
    String evaluated = """
      apiVersion: apps/v1 #1
      kind: StatefulSet #2
      metadata: #3
        name: helm-chart-sonarqube-dce-search #4
      spec: #5
        livenessProbe: #6
          exec: #7
            command: #8
              - sh #9
              - -c #10
              #14
              - | #15
                bar #16 #17
          initialDelaySeconds: 60 #18
        #19""";

    FileTree file = parseTemplate(evaluated);

    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  @Test
  void shouldNotCrashOnNewDocumentAfterEvaluation() throws IOException {
    // language=yaml
    var evaluated = """
      --- #5
      apiVersion: v1 #6
      kind: Pod #7
      metadata: #8
      spec: #9""";

    FileTree file = parseTemplate(evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  @Test
  void shouldRemoveLineNumberCommentForNewDocumentAtEndAfterEvaluation() throws IOException {
    // language=yaml
    var evaluated = """
      apiVersion: v1 #6
      kind: Pod #7
      metadata: #8
      spec: #9
      --- #12""";

    FileTree file = parseTemplate(evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  @Test
  void shouldRemoveLineNumberCommentForEndDocumentAfterEvaluation() throws IOException {
    var evaluated = """
      apiVersion: v1 #6
      kind: Pod #7
      metadata: #8
      spec: #9
      ... #10""";

    FileTree file = parseTemplate(evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  private FileTree parseTemplate(String evaluated) throws IOException {
    var valuesFile = mock(InputFile.class);
    when(valuesFile.filename()).thenReturn("values.yaml");
    when(valuesFile.contents()).thenReturn("foo: bar");
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);

    var processor = new TestHelmProcessor(evaluated);
    var helmParserLocal = new HelmParser(processor);
    return helmParserLocal.parseHelmFile(evaluated, inputFileContext);
  }
}
