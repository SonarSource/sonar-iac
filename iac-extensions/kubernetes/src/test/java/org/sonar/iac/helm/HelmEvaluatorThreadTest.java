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
package org.sonar.iac.helm;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.event.Level;
import org.sonar.api.impl.utils.DefaultTempFolder;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.ThreadUtils.activeCreatedThreadsName;

@Isolated
class HelmEvaluatorThreadTest {
  @TempDir
  static File tempDir;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @AfterAll
  static void cleanup() throws IOException {
    // workaround for Windows due to https://github.com/junit-team/junit5/issues/2811
    FileUtils.deleteDirectory(tempDir);
  }

  @Test
  void shouldNotLeakThread() throws IOException, InterruptedException {
    var threadsBefore = activeCreatedThreadsName();
    var helmEvaluator = new HelmEvaluator(new DefaultTempFolder(tempDir, false));
    helmEvaluator.start();
    helmEvaluator.initialize();

    var templateDependencies = Map.of("values.yaml", "container:\n  port: 8080", "Chart.yaml", "name: foo");
    helmEvaluator.evaluateTemplate("templates/my_file.yaml", "containerPort: {{ .Values.container.port }}", templateDependencies);
    helmEvaluator.stop();
    // Threads need some time to be released
    Thread.sleep(10);
    var threadsAfterStop = activeCreatedThreadsName();
    assertThat(threadsAfterStop).isEqualTo(threadsBefore);
    assertThat(logTester.logs(Level.DEBUG))
      .hasSize(1)
      .anyMatch(s -> s.startsWith("Preparing Helm analysis for platform: "));
  }
}
