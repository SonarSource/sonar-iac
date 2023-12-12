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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.sonar.api.impl.utils.DefaultTempFolder;
import org.sonar.iac.helm.utils.ExecutableHelper;
import org.sonar.iac.kubernetes.plugin.InstanceScopedHelmEvaluator;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class HelmEvaluatorTest {
  @TempDir
  File tempDir;

  @Test
  void shouldThrowIfRawEvaluationResultIsEmpty() {
    var helmEvaluator = new HelmEvaluator(tempDir);

    Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "", ""))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Empty evaluation result (serialization failed?)");
  }

  @Test
  void shouldThrowIfGoReturnsError() {
    var helmEvaluator = new InstanceScopedHelmEvaluator(new DefaultTempFolder(tempDir, false));

    Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "containerPort: {{ .Values.", "container:\n  port: 8080"))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrowOnDeserializationError() throws IOException {
    try (var ignored = mockStatic(TemplateEvaluationResult.class); var ignored2 = mockStatic(ExecutableHelper.class)) {
      when(TemplateEvaluationResult.parseFrom(any(byte[].class))).thenThrow(new InvalidProtocolBufferException("Invalid input"));
      var helmEvaluator = Mockito.spy(new HelmEvaluator(tempDir));
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[1]);
      var pb = mock(ProcessBuilder.class);
      when(pb.command()).thenReturn(Collections.emptyList());
      Mockito.doReturn(pb).when(helmEvaluator).prepareProcessBuilder(any(), anyLong());
      Mockito.doReturn(null).when(helmEvaluator).startProcess(any(), any(), any());

      Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "", ""))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Deserialization error");
    }
  }

  @Test
  void shouldEvaluateTemplate() throws IOException {
    var helmEvaluator = new HelmEvaluator(tempDir);

    var evaluationResult = helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "containerPort: {{ .Values.container.port }}", "container:\n  port: 8080");

    Assertions.assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
  }

  @Test
  void shouldEvaluateTemplateWithTrailingNewline() throws IOException {
    var helmEvaluator = new HelmEvaluator(tempDir);

    var evaluationResult = helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "containerPort: {{ .Values.container.port }}\n   \n", "container:\n  port: 8080");

    Assertions.assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
  }
}
