/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.docker.visitors;

import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SensorTelemetryMetrics;

public class DockerMetricsVisitor extends MetricsVisitor {

  public DockerMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, SensorTelemetryMetrics sensorTelemetryMetrics) {
    super(fileLinesContextFactory, noSonarFilter, sensorTelemetryMetrics);
  }
}
