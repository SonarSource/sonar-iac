/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.scanner.plugin.api.impl.utils.DefaultTempFolder;

public final class HelmEvaluatorTestUtils {

  /**
   * Tests running the real binary share the CI machine with other build tasks. This can make a run of the freshly
   * extracted binary slower than the production timeout, especially on Windows, and the killed process then looks like
   * an evaluation failure.
   */
  static final int TEST_PROCESS_TIMEOUT_SECONDS = 60;

  private HelmEvaluatorTestUtils() {
  }

  public static HelmEvaluator helmEvaluator(File workingDir) {
    return new HelmEvaluator(new DefaultTempFolder(workingDir, false), TEST_PROCESS_TIMEOUT_SECONDS);
  }
}
