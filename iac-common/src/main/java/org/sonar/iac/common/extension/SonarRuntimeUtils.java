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
package org.sonar.iac.common.extension;

import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.Version;

public final class SonarRuntimeUtils {

  public static final Version HIDDEN_FILES_SUPPORTED_API_VERSION = Version.create(12, 0);

  private SonarRuntimeUtils() {
    // Utility class
  }

  public static boolean isSonarLintContext(SonarRuntime sonarRuntime) {
    return sonarRuntime.getProduct() == SonarProduct.SONARLINT;
  }

  public static boolean isNotSonarLintContext(SonarRuntime sonarRuntime) {
    return !isSonarLintContext(sonarRuntime);
  }

  public static boolean isHiddenFilesAnalysisSupported(SonarRuntime sonarRuntime) {
    // Temporarily exclude SonarLint context, as it's breaking integration tests, where sonar-plugin-api is retrieved from the classpath, and
    // not from the SQ-IDE library
    // SonarLint is handling hidden files differently, so we still are able to analyze them without calling `descriptor.processesHiddenFiles()`
    return isNotSonarLintContext(sonarRuntime) && sonarRuntime.getApiVersion().isGreaterThanOrEqual(HIDDEN_FILES_SUPPORTED_API_VERSION);
  }

  public static void activateHiddenFilesProcessing(SonarRuntime sonarRuntime, SensorDescriptor descriptor) {
    if (isHiddenFilesAnalysisSupported(sonarRuntime)) {
      descriptor.processesHiddenFiles();
    }
  }
}
