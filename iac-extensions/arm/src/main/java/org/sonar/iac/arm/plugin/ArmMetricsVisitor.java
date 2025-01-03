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
package org.sonar.iac.arm.plugin;

import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SensorTelemetryMetrics;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;

import static org.sonar.iac.arm.plugin.ArmSensor.isBicepFile;

public class ArmMetricsVisitor extends YamlMetricsVisitor {
  protected ArmMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, SensorTelemetryMetrics sensorTelemetryMetrics) {
    super(fileLinesContextFactory, noSonarFilter, sensorTelemetryMetrics);
  }

  @Override
  protected boolean acceptFileForLoc(InputFileContext inputFileContext) {
    return !isBicepFile(inputFileContext);
  }

  @Override
  protected void languageSpecificMetrics() {
    register(SyntaxToken.class, defaultMetricsVisitor());
  }
}
