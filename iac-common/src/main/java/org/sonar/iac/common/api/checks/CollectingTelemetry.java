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
package org.sonar.iac.common.api.checks;

import org.sonar.iac.common.extension.visitors.SensorTelemetry;

/**
 * Implemented by checks that collect telemetry during analysis. Before the analysis starts, the language sensor injects
 * the shared {@link SensorTelemetry} into every such check via {@link #setSensorTelemetry(SensorTelemetry)}; the check
 * then records measures into it while analyzing, and they are reported once at the end of the project analysis by
 * {@code IacProjectSensor}.
 */
public interface CollectingTelemetry {

  void setSensorTelemetry(SensorTelemetry sensorTelemetry);
}
