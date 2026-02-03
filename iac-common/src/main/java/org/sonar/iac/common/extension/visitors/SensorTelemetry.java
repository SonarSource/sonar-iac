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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.Version;

public class SensorTelemetry {

  // Property key to enable log, currently using the DurationStatistics one
  private static final String PROPERTY_KEY = "sonar.iac.duration.statistics";
  // `.` should be used for telemetry groups and every IaC key should start with `iac.`
  private static final String KEY_PREFIX = "iac.";
  private static final Logger LOG = LoggerFactory.getLogger(SensorTelemetry.class);
  private static final Version TELEMETRY_SUPPORTED_API_VERSION = Version.create(10, 9);

  private long aggregatedLinesOfCode;
  private final List<Long> fileSizeList = new ArrayList<>();

  private final boolean recordStat;
  private final Map<String, String> telemetry = new HashMap<>();

  public SensorTelemetry(Configuration config) {
    recordStat = config.getBoolean(PROPERTY_KEY).orElse(false);
  }

  public void addLinesOfCode(int numberOfLines) {
    aggregatedLinesOfCode += numberOfLines;
  }

  public void addFileSize(long fileSize) {
    fileSizeList.add(fileSize);
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

  public void addAggregatedFileSizeTelemetry(String language) {
    if (!fileSizeList.isEmpty()) {
      long median = calculateMedian(fileSizeList);
      addTelemetry(language + ".files.count", String.valueOf(fileSizeList.size()));
      addTelemetry(language + ".files.medianSize", String.valueOf(median));
      addTelemetry(language + ".files.largestFiles", String.valueOf(getLargestNumbers(fileSizeList, 20)));
    }
  }

  public void reportTelemetry(SensorContext sensorContext) {
    var isTelemetrySupported = sensorContext.runtime().getApiVersion().isGreaterThanOrEqual(TELEMETRY_SUPPORTED_API_VERSION);
    if (isTelemetrySupported) {
      // addTelemetryProperty is added in 10.9:
      // https://github.com/SonarSource/sonar-plugin-api/releases/tag/10.9.0.2362
      telemetry.forEach((String key, String value) -> {
        if (recordStat) {
          LOG.debug("Reporting telemetry: {}={}", key, value);
        }
        sensorContext.addTelemetryProperty(key, value);
      });
    }
  }

  // Getter method for testing
  protected Map<String, String> getTelemetry() {
    return telemetry;
  }

  static long calculateMedian(List<Long> numbers) {
    if (numbers.isEmpty()) {
      return 0;
    }
    Collections.sort(numbers);
    int size = numbers.size();
    int middle = size / 2;
    if (size % 2 == 1) {
      return numbers.get(middle);
    } else {
      return (numbers.get(middle - 1) + numbers.get(middle)) / 2;
    }
  }

  static List<Long> getLargestNumbers(List<Long> numbers, int limit) {
    if (limit < 0) {
      return Collections.emptyList();
    }
    return numbers.stream()
      .sorted(Comparator.reverseOrder())
      .limit(limit)
      .toList();
  }
}
