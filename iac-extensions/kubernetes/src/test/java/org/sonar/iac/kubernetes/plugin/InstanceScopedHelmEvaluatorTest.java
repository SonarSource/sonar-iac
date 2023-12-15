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

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.impl.utils.DefaultTempFolder;

class InstanceScopedHelmEvaluatorTest {
  @TempDir
  static File tempDir;

  @AfterAll
  static void cleanup() throws IOException {
    // workaround for Windows due to https://github.com/junit-team/junit5/issues/2811
    FileUtils.deleteDirectory(tempDir);
  }

  @Test
  void shouldEvaluateTemplate() throws IOException {
    var helmEvaluator = new InstanceScopedHelmEvaluator(new DefaultTempFolder(tempDir, false));

    helmEvaluator.initialize();
    var evaluationResult = helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "containerPort: {{ .Values.container.port }}", "container:\n  port: 8080");

    Assertions.assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
  }
}
