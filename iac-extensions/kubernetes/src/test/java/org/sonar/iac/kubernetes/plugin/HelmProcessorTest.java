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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.TemplateEvaluationResult;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

    assertThat(logTester.logs(Level.DEBUG))
      .contains("Failed to initialize Helm evaluator, analysis of Helm files will be disabled");

    assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", Mockito.mock(InputFileContext.class)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");
  }

  @Test
  void shouldNotBeCalledIfHelmEvaluatorNotInitialized() throws IOException {
    var helmProcessor = new HelmProcessor(null);

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      var files = Map.of("values.yaml", valuesFile);
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      when(valuesFile.contents()).thenReturn("");
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = Mockito.mock(InputFileContext.class);

      assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");
    }
  }

  @Test
  void shouldThrowIfValuesFileNotFound() {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(Map.of());
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");

      assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext))
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
      var files = Map.of("values.yaml", badValuesFile);
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFile = Mockito.mock(InputFile.class);
      when(inputFile.toString()).thenReturn("chart/templates/foo.yaml");
      var inputFileContext = new InputFileContext(Mockito.mock(SensorContext.class), inputFile);

      assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Failed to evaluate Helm file chart/templates/foo.yaml: Failed to read file at chart/values.yaml");
    }
  }

  @Test
  void shouldNotThrowIfValuesFileIsEmpty() throws IOException {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var emptyValuesFile = Mockito.mock(InputFile.class);
      var files = Map.of("values.yaml", emptyValuesFile);
      when(emptyValuesFile.contents()).thenReturn("");
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");
      when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any()))
        .thenReturn(TemplateEvaluationResult.newBuilder().setTemplate("result: foo #1").build());

      var result = helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext);

      verify(helmEvaluator).evaluateTemplate(anyString(), anyString(), anyMap());
      assertThat(result).isEqualTo("result: foo #1");
    }
  }

  @Test
  void shouldNotThrowIfSomeFileIsEmpty() throws IOException {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      var someFile = Mockito.mock(InputFile.class);
      var files = Map.of("values.yaml", valuesFile, "templates/some.yaml", someFile);
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(someFile.contents()).thenReturn("kind: Pod");
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");
      when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any()))
        .thenReturn(TemplateEvaluationResult.newBuilder().setTemplate("result: foo #1").build());

      var result = helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext);

      verify(helmEvaluator).evaluateTemplate(anyString(), anyString(), anyMap());
      assertThat(result).isEqualTo("result: foo #1");
    }
  }

  @Test
  void shouldEvaluateTemplateAndReturnTemplate() throws IOException {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      when(valuesFile.contents()).thenReturn("container:\n  port: 8080");
      var files = Map.of("values.yaml", valuesFile);
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any()))
        .thenReturn(TemplateEvaluationResult.newBuilder().setTemplate("containerPort: 8080 #1").build());
      var inputFileContext = Mockito.mock(InputFileContext.class);

      var result = helmProcessor.processHelmTemplate("foo.yaml", "containerPort: {{ .Values.container.port }}", inputFileContext);

      assertEquals("containerPort: 8080 #1", result);
    }
  }

  @Test
  void shouldSkipHelmEvaluationIfHelmEvaluatorThrows() throws IOException {
    when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any())).thenThrow(new IllegalStateException("Failed to evaluate template"));
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      when(valuesFile.contents()).thenReturn("container:\n  port: 8080");
      var files = Map.of("values.yaml", valuesFile);
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");

      assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "containerPort: {{ .Values.container.port }}", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Failed to evaluate Helm file chart/templates/foo.yaml: Template evaluation failed");
    }
  }

  @Test
  void shouldNotEvaluateIfTemplateIsEmpty() throws IOException {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      var files = Map.of("values.yaml", valuesFile);
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");

      helmProcessor.processHelmTemplate("foo.yaml", "", inputFileContext);

      verify(helmEvaluator).initialize();
      verifyNoMoreInteractions(helmEvaluator);
      assertThat(logTester.logs(Level.DEBUG)).contains("The file chart/templates/foo.yaml is blank, skipping evaluation");
    }
  }

  @Test
  void shouldReadCompressedFile() throws IOException {
    var inputFile = inputFile("src/test/resources/helm/charts/common-0.0.1.tgz");
    var fileContents = HelmProcessor.readCompressedFile("charts/common-0.0.1.tgz", inputFile);
    var expectedKey = "charts/common/values.yaml".replace('/', File.separatorChar);
    assertThat(fileContents)
      .containsEntry(expectedKey,
        "# Default values\ncommonName: commonValue\n");
  }

  @Test
  void shouldThrowExceptionWhenNotCompressedFile() throws IOException {
    var inputFile = inputFile("src/test/resources/helm/Chart.yaml");

    assertThrowsExactly(ParseException.class,
      () -> HelmProcessor.readCompressedFile("charts/common-0.0.1.tgz", inputFile),
      "Failed to evaluate Helm file src/test/resources/helm/Chart.yaml: Failed to read compressed file");
  }

  public static List<String> zipSlipArchives() {
    List<String> names = new ArrayList<>();
    for (int i = 1; i < 2; i++) {
      names.add("evil" + i + "-unix.tar.gz");
      names.add("evil" + i + "-win.tar.gz");
    }
    return names;
  }

  @ParameterizedTest
  @MethodSource("zipSlipArchives")
  void shouldReadCompressedFileThtContainsZipSlip(String filename) throws IOException {
    var inputFile = inputFile("src/test/resources/helm/charts/" + filename);
    var fileContents = HelmProcessor.readCompressedFile("charts/" + filename, inputFile);
    assertThat(fileContents).isEmpty();
    assertThat(logTester.logs(Level.DEBUG))
      .contains("Read dependency chart charts/" + filename);
    assertThat(logTester.logs(Level.DEBUG).get(1))
      .startsWith("The path")
      .endsWith("in compressed file looks suspicious, ignoring the file");
  }

  @Test
  void shouldReadCompressedFileFilenameWithoutDirectory() throws IOException {
    var inputFile = inputFile("src/test/resources/helm/charts/common-0.0.1.tgz");
    var fileContents = HelmProcessor.readCompressedFile("common-0.0.1.tgz", inputFile);
    var expectedKey = "common-0.0.1.tgz/common/values.yaml".replace('/', File.separatorChar);
    assertThat(fileContents)
      .containsEntry(expectedKey,
        "# Default values\ncommonName: commonValue\n");
  }

  @Test
  void shouldEvaluateTemplateWithDependencyAndReturnTemplate() throws IOException {
    var helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      when(valuesFile.contents()).thenReturn("container:\n  port: 8080");
      var dependencyChart = inputFile("src/test/resources/helm/charts/common-0.0.1.tgz");
      var files = Map.of("values.yaml", valuesFile,
        "charts/common-0.0.1.tgz", dependencyChart);
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any()))
        .thenReturn(TemplateEvaluationResult.newBuilder().setTemplate("containerPort: 8080 #1").build());
      var inputFileContext = Mockito.mock(InputFileContext.class);

      var result = helmProcessor.processHelmTemplate("foo.yaml", "containerPort: {{ .Values.container.port }}", inputFileContext);

      assertEquals("containerPort: 8080 #1", result);
    }
  }

  private static InputFileContext mockInputFileContext(String filename) {
    var inputFile = Mockito.mock(InputFile.class);
    when(inputFile.newPointer(anyInt(), anyInt())).thenReturn(new BasicTextPointer(0, 0));
    when(inputFile.toString()).thenReturn(filename);
    return new InputFileContext(Mockito.mock(SensorContext.class), inputFile);
  }

  private InputFile inputFile(String relativePath) throws IOException {
    var in = new FileInputStream(relativePath);
    var inputFile = mock(InputFile.class);
    when(inputFile.inputStream()).thenReturn(in);
    when(inputFile.toString()).thenReturn(relativePath);
    return inputFile;
  }
}
