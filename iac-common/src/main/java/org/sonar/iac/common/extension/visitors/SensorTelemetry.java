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
package org.sonar.iac.common.extension.visitors;

import java.util.HashMap;
import java.util.Map;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;

public class SensorTelemetry {

  // `.` should be used for telemetry groups and every IaC key should start with `iac.`
  private static final String KEY_PREFIX = "iac.";
  private static final Version TELEMETRY_SUPPORTED_API_VERSION = Version.create(10, 9);
  private long aggregatedLinesOfCode;

  private final Map<String, String> telemetry = new HashMap<>();

  public SensorTelemetry() {
    // Empty constructor
  }

  public void addLinesOfCode(int numberOfLines) {
    aggregatedLinesOfCode += numberOfLines;
  }

  public void addTelemetry(String key, String value) {
    key = KEY_PREFIX + key;
    if (telemetry.containsKey(key)) {
      throw new IllegalArgumentException("telemetry key is reported more than once: " + key);
    }
    telemetry.put(key, value);
  }

  public void addAggregatedLinesOfCodeTelemetry(String language) {
    if (aggregatedLinesOfCode > 0) {
      addTelemetry(language + ".loc", String.valueOf(aggregatedLinesOfCode));
    }
  }

  public void reportTelemetry(SensorContext sensorContext) {
    var isTelemetrySupported = sensorContext.runtime().getApiVersion().isGreaterThanOrEqual(TELEMETRY_SUPPORTED_API_VERSION);
    if (isTelemetrySupported) {
      // addTelemetryProperty is added in 10.9:
      // https://github.com/SonarSource/sonar-plugin-api/releases/tag/10.9.0.2362
      telemetry.forEach(sensorContext::addTelemetryProperty);
    }
  }

  // Getter method for testing
  protected Map<String, String> getTelemetry() {
    return telemetry;
  }
}
