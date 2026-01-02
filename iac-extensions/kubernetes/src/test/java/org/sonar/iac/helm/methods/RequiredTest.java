/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.helm.methods;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
    this.helmEvaluator.start();
  }

  @AfterEach
  void free() {
    this.helmEvaluator.stop();
  }

  @AfterAll
  static void cleanup() throws IOException, InterruptedException {
    // https://stackoverflow.com/questions/64090643/java-nio-file-accessdeniedexception-while-trying-to-delete-a-renamed-directoryu
    System.gc();
    Thread.sleep(1000);
    // workaround for Windows due to https://github.com/junit-team/junit5/issues/2811
    FileUtils.deleteDirectory(tempDir);
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
