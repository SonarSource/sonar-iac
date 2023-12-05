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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.iac.helm.jna.Loader;
import org.sonar.iac.helm.jna.library.IacHelmLibrary;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

import static org.mockito.ArgumentMatchers.any;

class HelmEvaluatorTest {
  @Test
  void shouldThrowIfRawEvaluationResultIsEmpty() {
    // Mock methods that are executed by JNA in runtime
    var emptyRawResult = Mockito.mock(IacHelmLibrary.EvaluateTemplate_return.ByValue.class);
    Mockito.when(emptyRawResult.getByteArray()).thenReturn(new byte[0]);
    var iacHelmLibrary = Mockito.mock(IacHelmLibrary.class);
    Mockito.when(iacHelmLibrary.evaluateTemplate(any(), any(), any())).thenReturn(emptyRawResult);
    var helmEvaluator = new HelmEvaluator(iacHelmLibrary);

    Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/helm/templates/pod.yaml", "", ""))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Empty evaluation result (serialization failed?)");
  }

  @Test
  void shouldThrowIfGoReturnsError() {
    var iacHelmLibrary = (new Loader()).load("/sonar-helm-for-iac", IacHelmLibrary.class);
    var helmEvaluator = new HelmEvaluator(iacHelmLibrary);

    Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/helm/templates/pod.yaml", "containerPort: {{ .Values.", "container:\n  port: 8080"))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrowOnDeserializationError() throws InvalidProtocolBufferException {
    try (var ignored = Mockito.mockStatic(TemplateEvaluationResult.class)) {
      Mockito.when(TemplateEvaluationResult.parseFrom(any(byte[].class))).thenThrow(new InvalidProtocolBufferException("Invalid input"));
      var iacHelmLibrary = Mockito.mock(IacHelmLibrary.class);
      var helmEvaluator = new HelmEvaluator(iacHelmLibrary);
      var rawEvaluationResult = Mockito.mock(IacHelmLibrary.EvaluateTemplate_return.ByValue.class);
      Mockito.when(iacHelmLibrary.evaluateTemplate(any(), any(), any())).thenReturn(rawEvaluationResult);
      Mockito.when(rawEvaluationResult.getByteArray()).thenReturn(new byte[1]);

      Assertions.assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/helm/templates/pod.yaml", "", ""))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Deserialization error");
    }
  }

  @Test
  void shouldEvaluateTemplate() {
    var iacHelmLibrary = (new Loader()).load("/sonar-helm-for-iac", IacHelmLibrary.class);
    var helmEvaluator = new HelmEvaluator(iacHelmLibrary);

    var evaluationResult = helmEvaluator.evaluateTemplate("/helm/templates/pod.yaml", "containerPort: {{ .Values.container.port }}", "container:\n  port: 8080");

    Assertions.assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
  }
}
