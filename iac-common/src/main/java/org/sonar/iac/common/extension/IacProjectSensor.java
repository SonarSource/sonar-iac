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
package org.sonar.iac.common.extension;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;

/**
 * A single {@link ProjectSensor} that is instantiated only once per project.
 * It holds a single shared {@link SensorTelemetry} instance used by all IaC sensors
 * and reports telemetry at the end of the project analysis.
 * This is the <b>only</b> place where {@link SensorContext#addTelemetryProperty} is called.
 */
public class IacProjectSensor implements ProjectSensor {

  private static final Version TELEMETRY_SUPPORTED_API_VERSION = Version.create(10, 9);
  // Property key to enable log, currently using the DurationStatistics one
  private static final String PROPERTY_KEY = "sonar.iac.duration.statistics";

  private static final Logger LOG = LoggerFactory.getLogger(IacProjectSensor.class);

  private final SensorTelemetry sensorTelemetry;
  private final boolean recordStat;

  public IacProjectSensor(Configuration configuration) {
    sensorTelemetry = new SensorTelemetry();
    recordStat = configuration.getBoolean(PROPERTY_KEY).orElse(false);
  }

  /**
   * Returns the single shared {@link SensorTelemetry} instance for this project.
   * All language sensors share this instance; per-language data is tracked internally
   * using language keys.
   */
  public SensorTelemetry getSensorTelemetry() {
    return sensorTelemetry;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("IaC Project Sensor");
  }

  @Override
  public void execute(SensorContext context) {
    if (!isTelemetrySupported(context)) {
      return;
    }
    var sortedEntries = sensorTelemetry.getTelemetry()
      .entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .toList();
    if (recordStat) {
      sortedEntries.forEach(entry -> LOG.debug("Reporting telemetry: {}={}", entry.getKey(), entry.getValue()));
    }
    sortedEntries.forEach(entry -> context.addTelemetryProperty(entry.getKey(), entry.getValue()));
  }

  private static boolean isTelemetrySupported(SensorContext sensorContext) {
    return sensorContext.runtime().getApiVersion().isGreaterThanOrEqual(TELEMETRY_SUPPORTED_API_VERSION);
  }
}
