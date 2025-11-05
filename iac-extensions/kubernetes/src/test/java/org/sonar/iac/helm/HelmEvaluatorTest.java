/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.helm;

import com.google.protobuf.InvalidProtocolBufferException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.impl.utils.DefaultTempFolder;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.helm.protobuf.TemplateEvaluationResult;
import org.sonar.iac.helm.utils.ExecutableHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class HelmEvaluatorTest {
  @TempDir
  static File tempDir;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private HelmEvaluator helmEvaluator;

  @BeforeEach
  void setUp() throws IOException {
    this.helmEvaluator = new HelmEvaluator(new DefaultTempFolder(tempDir, false));
    this.helmEvaluator.start();
    this.helmEvaluator.initialize();
  }

  @AfterEach
  void release() {
    helmEvaluator.stop();
  }

  @AfterAll
  static void cleanup() throws IOException {
    // workaround for Windows due to https://github.com/junit-team/junit5/issues/2811
    FileUtils.deleteDirectory(tempDir);
  }

  @RetryingTest(maxAttempts = 3)
  void shouldThrowIfGoBinaryNotFoundChartYaml() {
    var templateDependencies = Map.<String, String>of();
    assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Evaluation error in Go library: source file Chart.yaml not found");

    var logs = logTester.logs(Level.DEBUG).stream().filter(log -> !log.startsWith("Preparing Helm analysis for platform")).toList();
    assertThat(logs)
      .contains("[sonar-helm-for-iac] Exception encountered, printing recorded logs");
  }

  @Test
  void shouldThrowIfGoBinaryReturnsNonZero() throws IOException {
    try (var ignored = Mockito.mockStatic(ExecutableHelper.class)) {
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[0]);
      var helmEvaluatorSpy = Mockito.spy(this.helmEvaluator);
      var process = mock(Process.class);
      when(process.isAlive()).thenReturn(false);
      when(process.exitValue()).thenReturn(1);
      doReturn(process).when(helmEvaluatorSpy).startProcess();
      doNothing().when(helmEvaluatorSpy).writeTemplateAndDependencies(any(), any(), any(), any());

      var templateDependencies = Map.<String, String>of();
      assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("sonar-helm-for-iac exited with non-zero exit code: 1, possible serialization failure");
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldThrowIfRawEvaluationResultIsEmptyOrNull(boolean isNull) throws IOException {
    var expectedBytes = isNull ? null : new byte[0];
    try (var ignored = Mockito.mockStatic(ExecutableHelper.class)) {
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(expectedBytes);
      var helmEvaluatorSpy = Mockito.spy(this.helmEvaluator);
      var process = mock(Process.class);
      when(process.isAlive()).thenReturn(false);
      when(process.exitValue()).thenReturn(0);
      doReturn(process).when(helmEvaluatorSpy).startProcess();
      doNothing().when(helmEvaluatorSpy).writeTemplateAndDependencies(any(), any(), any(), any());

      var templateDependencies = Map.of("values.yaml", "");
      assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Empty evaluation result returned from sonar-helm-for-iac");
    }
  }

  @Test
  void shouldThrowIfGoReturnsError() {
    var templateDependencies = Map.of("values.yaml", "container:\n  port: 8080");
    assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "containerPort: {{ .Values.", templateDependencies))
      .isInstanceOf(IllegalStateException.class);
    assertThat(logTester.logs(Level.DEBUG))
      .contains("[sonar-helm-for-iac]   Reading 26 bytes of file /foo/bar/baz.yaml from stdin");
  }

  @Test
  void shouldThrowOnDeserializationError() throws IOException {
    try (var ignored = mockStatic(TemplateEvaluationResult.class); var ignored2 = mockStatic(ExecutableHelper.class)) {
      when(TemplateEvaluationResult.parseFrom(any(byte[].class))).thenThrow(new InvalidProtocolBufferException("Invalid input"));
      var helmEvaluatorSpy = Mockito.spy(this.helmEvaluator);
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[1]);
      var pb = mock(ProcessBuilder.class);
      when(pb.command()).thenReturn(Collections.emptyList());
      Mockito.doReturn(pb).when(helmEvaluatorSpy).prepareProcessBuilder();
      Mockito.doReturn(null).when(helmEvaluatorSpy).startProcess();
      doNothing().when(helmEvaluatorSpy).writeTemplateAndDependencies(any(), any(), any(), any());

      var templateDependencies = Map.<String, String>of();
      assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Deserialization error");
    }
  }

  @Test
  void shouldEvaluateTemplate() throws IOException {
    var templateDependencies = Map.of("values.yaml", "container:\n  port: 8080", "Chart.yaml", "name: foo");
    var evaluationResult = helmEvaluator.evaluateTemplate("templates/baz.yaml", "containerPort: {{ .Values.container.port }}", templateDependencies);

    assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
    assertThat(logTester.logs()).noneMatch(log -> log.startsWith("[sonar-helm-for-iac]"));
  }

  @Test
  void shouldEvaluateInputsWithTrailingNewline() throws IOException {
    var evaluationResult = helmEvaluator.evaluateTemplate("templates/baz.yaml", "containerPort: {{ .Values.container.port }}\n   \n",
      Map.of("values.yaml", "container:\n  port: 8080\n\n", "Chart.yaml", "name: foo\n\n"));

    assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
    assertThat(logTester.logs())
      .anyMatch(log -> log.startsWith("Preparing Helm analysis for platform:"))
      .noneMatch(log -> log.startsWith("[sonar-helm-for-iac]"));
  }

  @ParameterizedTest
  @CsvSource({
    "0,00 00 00 00",
    "1,00 00 00 01",
    "10,00 00 00 0A",
    "255,00 00 00 FF",
    "256,00 00 01 00",
    "21812,00 00 55 34",
    "65535,00 00 FF FF",
    "65536,00 01 00 00",
    Integer.MAX_VALUE + ",7F FF FF FF"})
  void shouldConvertIntToBytes(int number, String expected) {
    var bytes = HelmEvaluator.intTo4Bytes(number);
    var asText = "%02X %02X %02X %02X".formatted(bytes[0], bytes[1], bytes[2], bytes[3]);
    assertThat(asText).isEqualTo(expected);
  }
}
