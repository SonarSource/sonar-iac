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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class HelmProcessorTest {
  private final HelmEvaluator helmEvaluator = Mockito.mock(HelmEvaluator.class);

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @Test
  void shouldHandleInitializationError() throws IOException {
    doThrow(new IOException("Failed to initialize Helm evaluator")).when(helmEvaluator).initialize();
    var helmProcessor = new HelmProcessor(helmEvaluator);

    helmProcessor.initialize();

    Assertions.assertThat(logTester.logs(Level.DEBUG))
      .contains("Failed to initialize Helm evaluator, analysis of Helm files will be disabled");

    Assertions.assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", Mockito.mock(InputFileContext.class)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");
  }

  @Test
  void shouldNotBeCalledIfHelmEvaluatorNotInitialized() throws IOException {
    var helmProcessor = new HelmProcessor(null);

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      when(valuesFile.contents()).thenReturn("");
      when(HelmFilesystemUtils.findValuesFile(any())).thenReturn(valuesFile);
      var inputFileContext = Mockito.mock(InputFileContext.class);

      Assertions.assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");
    }
  }

  @Test
  void shouldThrowIfValuesFileNotFound() {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      when(HelmFilesystemUtils.findValuesFile(any())).thenReturn(null);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");

      Assertions.assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext))
        .isInstanceOf(ParseException.class);
    }
  }

  @Test
  void shouldThrowIfValuesFileNotRead() throws IOException, URISyntaxException {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var badValuesFile = Mockito.mock(InputFile.class);
      when(badValuesFile.contents()).thenThrow(new IOException("Failed to read values file"));
      when(badValuesFile.uri()).thenReturn(new URI("file:///projects/chart/values.yaml"));
      when(badValuesFile.toString()).thenReturn("chart/values.yaml");
      when(HelmFilesystemUtils.findValuesFile(any())).thenReturn(badValuesFile);
      var inputFile = Mockito.mock(InputFile.class);
      when(inputFile.toString()).thenReturn("chart/templates/foo.yaml");
      var inputFileContext = new InputFileContext(Mockito.mock(SensorContext.class), inputFile);

      Assertions.assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Failed to read values file at chart/values.yaml while evaluating Helm file chart/templates/foo.yaml");
    }
  }

  @Test
  void shouldThrowIfValuesFileIsEmpty() throws IOException {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var badValuesFile = Mockito.mock(InputFile.class);
      when(badValuesFile.contents()).thenReturn("");
      when(HelmFilesystemUtils.findValuesFile(any())).thenReturn(badValuesFile);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");

      Assertions.assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext))
        .isInstanceOf(ParseException.class);
    }
  }

  @Test
  void shouldEvaluateTemplateAndReturnTemplate() throws IOException {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      when(valuesFile.contents()).thenReturn("container:\n  port: 8080");
      when(HelmFilesystemUtils.findValuesFile(any())).thenReturn(valuesFile);
      when(helmEvaluator.evaluateTemplate(anyString(), anyString(), anyString()))
        .thenReturn(TemplateEvaluationResult.newBuilder().setTemplate("containerPort: 8080 #1").build());
      var inputFileContext = Mockito.mock(InputFileContext.class);

      var result = helmProcessor.processHelmTemplate("foo.yaml", "containerPort: {{ .Values.container.port }}", inputFileContext);

      assertEquals("containerPort: 8080 #1", result);
    }
  }

  @Test
  void shouldSkipHelmEvaluationIfHelmEvaluatorThrows() throws IOException {
    when(helmEvaluator.evaluateTemplate(anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("Failed to evaluate template"));
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      when(valuesFile.contents()).thenReturn("container:\n  port: 8080");
      when(HelmFilesystemUtils.findValuesFile(any())).thenReturn(valuesFile);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");

      Assertions.assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "containerPort: {{ .Values.container.port }}", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Template evaluation failed, skipping processing of Helm file foo.yaml");
    }
  }

  private static InputFileContext mockInputFileContext(String filename) {
    var inputFile = Mockito.mock(InputFile.class);
    when(inputFile.newPointer(anyInt(), anyInt())).thenReturn(new BasicTextPointer(0, 0));
    when(inputFile.toString()).thenReturn(filename);
    return new InputFileContext(Mockito.mock(SensorContext.class), inputFile);
  }
}
