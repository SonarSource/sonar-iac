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
package org.sonar.iac.kubernetes.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.predicates.DefaultFilePredicates;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.FileTree;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KubernetesParserTest {
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private final InputFile inputFile = mock(InputFile.class);
  private final SensorContext sensorContext = mock(SensorContext.class);
  private final InputFileContext inputFileContext = new InputFileContext(sensorContext, inputFile);

  private final KubernetesParser parser = new KubernetesParser(new HelmProcessor());

  @BeforeEach
  void setup() {
    var fs = mock(FileSystem.class);
    when(sensorContext.fileSystem()).thenReturn(fs);
    when(fs.predicates()).thenReturn(new DefaultFilePredicates(Path.of(".")));
    when(inputFile.filename()).thenReturn("foo.yaml");
  }

  @Test
  void testParsingWhenHelmContentIsDetected() {
    FileTree file = parser.parse("foo: {{ .Value.var }}", inputFileContext);
    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isNotEmpty();
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file 'foo.yaml'");
  }

  @Test
  void testParsingWhenHelmContentIsDetectedNoInputFileContext() {
    FileTree file = parser.parse("foo: {{ .Value.var }}", null);
    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isNotEmpty();
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("No InputFileContext provided, skipping processing of Helm file");
  }

  @Test
  void testParsingWhenNoHelmContent() {
    FileTree file = parser.parse("foo: {bar: 1234}", inputFileContext);
    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isNotEmpty();
    assertThat(file.template()).isEqualTo(FileTree.Template.NONE);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).isEmpty();
  }

  @Test
  void shouldLoadValuesFile() throws IOException {
    var valuesFile = mock(InputFile.class);
    when(valuesFile.filename()).thenReturn("values.yaml");
    when(valuesFile.contents()).thenReturn("foo: bar");
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
    FileTree file = parser.parse("foo: {{ .Values.foo }}", inputFileContext);
    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).hasSize(1);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file 'foo.yaml'");
  }

  @Test
  void shouldNotEvaluateHelmWithoutValuesFile() throws IOException {
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(null);
    FileTree file = parser.parse("foo: {{ .Values.foo }}", inputFileContext);
    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isEmpty();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file 'foo.yaml'")
      .contains("Failed to read values file, skipping processing of Helm file 'foo.yaml'");
  }

}
