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
package org.sonar.iac.common.extension.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds telemetry data collected during analysis. A single instance is shared across all IaC sensors via
 * {@link org.sonar.iac.common.extension.IacProjectSensor#getSensorTelemetry()} so values accumulate across
 * multi-module projects (where {@code Sensor} implementations can be instantiated multiple times). It is also injected
 * into every check that implements {@link org.sonar.iac.common.api.checks.CollectingTelemetry}, so checks can
 * contribute measures directly while analyzing.
 * <p>
 * Telemetry can be added through the following methods:
 * <ul>
 *   <li>{@link #addNumericalMeasure(String, long)} – sums values across calls (multi-module aggregation).</li>
 *   <li>{@link #addLinesOfCode(String, int)} / {@link #addFileSize(String, long)} – per-language complex aggregations.</li>
 *   <li>{@link #setNumericalMeasure(String, long)} – overwrites the previous value (use only for project-level singletons).</li>
 *   <li>{@link #setBooleanMeasure(String, boolean)} – ORs values across calls; produces {@code "1"} when any caller
 *       supplied {@code true}, {@code "0"} when at least one call supplied a value but all were {@code false}.</li>
 * </ul>
 * Callers are expected to construct keys without the {@code "iac."} prefix, which is added automatically. Free-form
 * values that become part of a key must first be passed through {@link #sanitizeKeySegment(String)}.
 * <p>
 * Because the single instance is shared by every sensor and check, the backing stores are {@link ConcurrentHashMap}s
 * whose numeric, boolean and lines-of-code entries are mutated only through atomic {@code merge}/{@code computeIfAbsent}.
 * The per-language file-size lists are synchronized (and snapshotted before they are read in {@link #getTelemetry()}),
 * so contribution stays safe even if analysis ever runs checks concurrently.
 */
public class SensorTelemetry {

  // `.` should be used for telemetry groups and every IaC key should start with `iac.`
  private static final String KEY_PREFIX = "iac.";

  // Numerical measures (sum or set semantics) populated by domain-specific sensors and checks
  private final Map<String, Long> numericalMeasures = new ConcurrentHashMap<>();

  // Boolean measures (OR semantics) populated by domain-specific sensors and checks
  private final Map<String, Boolean> booleanMeasures = new ConcurrentHashMap<>();

  // Per-language lines of code accumulated across calls
  private final Map<String, Long> linesOfCodePerLanguage = new ConcurrentHashMap<>();

  // Per-language file sizes accumulated across calls
  private final Map<String, List<Long>> fileSizesPerLanguage = new ConcurrentHashMap<>();

  /**
   * Accumulates lines of code for a given language. Stored at key {@code iac.<language>.loc}.
   * Negative or zero values are ignored. Repeated calls sum the values.
   */
  public void addLinesOfCode(String language, int numberOfLines) {
    if (numberOfLines <= 0) {
      return;
    }
    linesOfCodePerLanguage.merge(language, (long) numberOfLines, Long::sum);
  }

  /**
   * Tracks a file size for a given language. The per-language file statistics are published under keys
   * {@code iac.<language>.files.count},
   * {@code iac.<language>.files.medianSize} and
   * {@code iac.<language>.files.largestFiles} when {@link #getTelemetry()} is called.
   */
  public void addFileSize(String language, long fileSize) {
    // Synchronized list so concurrent add() calls for the same language are safe; getTelemetry() snapshots before reading.
    fileSizesPerLanguage.computeIfAbsent(language, k -> Collections.synchronizedList(new ArrayList<>())).add(fileSize);
  }

  /**
   * Adds a numerical measure under {@code iac.<key>}. Repeated calls with the same key sum the values.
   * Use this for measures aggregated across modules (file counts, issue counts, etc.).
   */
  public void addNumericalMeasure(String key, long value) {
    numericalMeasures.merge(KEY_PREFIX + key, value, Long::sum);
  }

  /**
   * Sets a numerical measure under {@code iac.<key>}, overwriting any previous value. Use this only for
   * project-level singletons whose values are already cumulative across modules (e.g. counts read from a
   * {@link org.sonar.api.scanner.ScannerSide} provider).
   */
  public void setNumericalMeasure(String key, long value) {
    numericalMeasures.put(KEY_PREFIX + key, value);
  }

  /**
   * Sets a boolean measure under {@code iac.<key>}. Repeated calls with the same key OR the values:
   * any call with {@code true} produces {@code "1"}; if all calls supplied {@code false}, the result is {@code "0"}.
   */
  public void setBooleanMeasure(String key, boolean value) {
    booleanMeasures.merge(KEY_PREFIX + key, value, (a, b) -> a || b);
  }

  /**
   * Sanitizes a free-form value so it can be safely embedded as a single segment of a telemetry key: every run of
   * characters other than an ASCII letter or digit is collapsed to a single {@code _}, and leading/trailing {@code _}
   * are trimmed. Callers must apply this to any user-controlled value before appending it to a key, so keys never
   * contain spaces, the {@code .} group separator or other characters that telemetry backends may restrict. The result
   * stays human-readable, e.g. {@code Storage Blob Data Contributor} becomes {@code Storage_Blob_Data_Contributor}.
   */
  public static String sanitizeKeySegment(String segment) {
    return segment.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_++", "").replaceAll("_++$", "");
  }

  public Map<String, String> getTelemetry() {
    var telemetry = new HashMap<String, String>();
    numericalMeasures.forEach((key, value) -> telemetry.put(key, String.valueOf(value)));
    booleanMeasures.forEach((key, value) -> telemetry.put(key, Boolean.TRUE.equals(value) ? "1" : "0"));
    linesOfCodePerLanguage.forEach((language, loc) -> telemetry.put(KEY_PREFIX + language + ".loc", String.valueOf(loc)));
    fileSizesPerLanguage.forEach((language, sizes) -> {
      // Snapshot the synchronized list before reading; new ArrayList<>(sizes) synchronizes on the list internally.
      var sizesSnapshot = new ArrayList<>(sizes);
      telemetry.put(KEY_PREFIX + language + ".files.medianSize", String.valueOf(calculateMedian(sizesSnapshot)));
      telemetry.put(KEY_PREFIX + language + ".files.largestFiles", String.valueOf(getLargestNumbers(sizesSnapshot, 20)));
    });
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
