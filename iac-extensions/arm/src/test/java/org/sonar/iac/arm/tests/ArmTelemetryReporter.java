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
package org.sonar.iac.arm.tests;

import java.io.File;
import java.util.Map;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.arm.plugin.ArmParserStatistics;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;

public class ArmTelemetryReporter {
  public static Map<String, String> storeTelemetryAndReport(ArmParserStatistics statistics) {
    var sensorContext = SensorContextTester.create(new File("."));
    var telemetry = new SensorTelemetry(sensorContext.config());
    statistics.storeTelemetry(telemetry);
    telemetry.reportTelemetry(sensorContext);
    return sensorContext.getTelemetryProperties();
  }
}
