/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.extension;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class DurationStatistics {

  private static final Logger LOG = Loggers.get(DurationStatistics.class);

  private static final String PROPERTY_KEY = "sonar.iac.duration.statistics";

  private final Map<String, AtomicLong> stats = new ConcurrentHashMap<>();

  private final boolean recordStat;

  public DurationStatistics(Configuration config) {
    recordStat = config.getBoolean(PROPERTY_KEY).orElse(false);
  }

  public <C, T> BiConsumer<C, T> time(String id, BiConsumer<C, T> consumer) {
    if (recordStat) {
      return (t, u) -> time(id, () -> consumer.accept(t, u));
    } else {
      return consumer;
    }
  }

  public void time(String id, Runnable runnable) {
    if (recordStat) {
      time(id, () -> {
        runnable.run();
        return null;
      });
    } else {
      runnable.run();
    }
  }

  public <T> T time(String id, Supplier<T> supplier) {
    if (recordStat) {
      long startTime = System.nanoTime();
      T result = supplier.get();
      addRecord(id, System.nanoTime() - startTime);
      return result;
    } else {
      return supplier.get();
    }
  }

  void addRecord(String id, long elapsedTime) {
    stats.computeIfAbsent(id, key -> new AtomicLong(0)).addAndGet(elapsedTime);
  }

  public void log() {
    if (recordStat) {
      StringBuilder out = new StringBuilder();
      DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
      symbols.setGroupingSeparator('\'');
      NumberFormat format = new DecimalFormat("#,###", symbols);
      out.append("Duration Statistics");
      stats.entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
        .forEach(e -> out.append(", ")
          .append(e.getKey())
          .append(" ")
          .append(format.format(e.getValue().get() / 1_000_000L))
          .append(" ms"));
      LOG.info(out.toString());
    }
  }
}
