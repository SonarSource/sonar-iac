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
package org.sonar.iac.helm;

import com.google.protobuf.InvalidProtocolBufferException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.impl.utils.DefaultTempFolder;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.helm.utils.ExecutableHelper;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

import static org.mockito.ArgumentMatchers.any;
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
    this.helmEvaluator.initialize();
  }

  @AfterAll
  static void cleanup() throws IOException {
    // workaround for Windows due to https://github.com/junit-team/junit5/issues/2811
    FileUtils.deleteDirectory(tempDir);
  }

  @Test
  void shouldThrowIfGoBinaryRejectsEmptyInput() {
    var templateDependencies = Map.<String, String>of();
    Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Evaluation error in Go library: error reading content: request to read 0 lines aborted");

    Assertions.assertThat(logTester.logs(Level.DEBUG))
      .contains("[sonar-helm-for-iac] Reading 0 lines from stdin");
  }

  @Test
  void shouldThrowIfGoBinaryReturnsNonZero() throws IOException {
    try (var ignored = Mockito.mockStatic(ExecutableHelper.class)) {
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[0]);
      var helmEvaluator = Mockito.spy(this.helmEvaluator);
      var process = mock(Process.class);
      when(process.isAlive()).thenReturn(false);
      when(process.exitValue()).thenReturn(1);
      doReturn(process).when(helmEvaluator).startProcess(any(), any(), any());

      var templateDependencies = Map.<String, String>of();
      Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
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
      var helmEvaluator = Mockito.spy(this.helmEvaluator);
      var process = mock(Process.class);
      when(process.isAlive()).thenReturn(false);
      when(process.exitValue()).thenReturn(0);
      doReturn(process).when(helmEvaluator).startProcess(any(), any(), any());

      var templateDependencies = Map.of("values.yaml", "");
      Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Empty evaluation result returned from sonar-helm-for-iac");
    }
  }

  @Test
  void shouldThrowIfGoReturnsError() {
    var templateDependencies = Map.of("values.yaml", "container:\n  port: 8080");
    Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "containerPort: {{ .Values.", templateDependencies))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrowOnDeserializationError() throws IOException {
    try (var ignored = mockStatic(TemplateEvaluationResult.class); var ignored2 = mockStatic(ExecutableHelper.class)) {
      when(TemplateEvaluationResult.parseFrom(any(byte[].class))).thenThrow(new InvalidProtocolBufferException("Invalid input"));
      var helmEvaluator = Mockito.spy(this.helmEvaluator);
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[1]);
      var pb = mock(ProcessBuilder.class);
      when(pb.command()).thenReturn(Collections.emptyList());
      Mockito.doReturn(pb).when(helmEvaluator).prepareProcessBuilder();
      Mockito.doReturn(null).when(helmEvaluator).startProcess(any(), any(), any());

      var templateDependencies = Map.<String, String>of();
      Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Deserialization error");
    }
  }

  @Test
  void shouldEvaluateTemplate() throws IOException {
    var templateDependencies = Map.of("values.yaml", "container:\n  port: 8080", "Chart.yaml", "name: foo");
    var evaluationResult = helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "containerPort: {{ .Values.container.port }}", templateDependencies);

    Assertions.assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
  }

  @Test
  void shouldEvaluateInputsWithTrailingNewline() throws IOException {
    var evaluationResult = helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "containerPort: {{ .Values.container.port }}\n   \n",
      Map.of("values.yaml", "container:\n  port: 8080\n\n", "Chart.yaml", "name: foo\n\n"));

    Assertions.assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
  }
}
