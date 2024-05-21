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
package org.sonar.iac.helm.methods;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.impl.utils.DefaultTempFolder;
import org.sonar.iac.helm.HelmEvaluator;

class RequiredTest {
  @TempDir
  static File tempDir;

  private HelmEvaluator helmEvaluator;

  @BeforeEach
  void setUp() throws IOException {
    this.helmEvaluator = new HelmEvaluator(new DefaultTempFolder(tempDir, false));
    this.helmEvaluator.initialize();
  }

  @Test
  void requireWithoutValueShouldThrowIllegalStateException() {
    var templateDependencies = Map.of("values.yaml", "var1: myVal", "Chart.yaml", "name: foo");
    Assertions
      .assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("templates/baz.yaml", "field: {{ required \"Missing mandatory var2 values!\" .Values.var2 }}", templateDependencies))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("error calling required: Missing mandatory var2 values!");
  }

  @Test
  void requireWithValueShouldBeProcessedNormally() throws IOException {
    var templateDependencies = Map.of("values.yaml", "var1: myVal", "Chart.yaml", "name: foo");
    var evaluationResult = helmEvaluator.evaluateTemplate("templates/baz.yaml", "field: {{ required \"Missing mandatory var2 values!\" .Values.var1 }}", templateDependencies);

    Assertions.assertThat(evaluationResult.getTemplate()).contains("field: myVal");
  }
}
