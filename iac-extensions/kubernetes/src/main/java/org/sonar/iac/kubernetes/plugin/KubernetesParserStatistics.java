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
package org.sonar.iac.kubernetes.plugin;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

/**
 * Fields of this class:
 * <ul>
 *   <li>{@code pureKubernetesParsedFileCount} <= {@code pureKubernetesFileCount} files that are not part of a Helm project</li>
 *   <li>{@code helmParsedFileCount} <= {@code helmFileCount} files that are part of a Helm project</li>
 *   <li>{@code kustomizeFileCount} number of {@code kustomize.y[a]ml} encountered in a whole file system project</li>
 * </ul>
 * Usually kustomization files doesn't contain Kubernetes identifiers so they are not parsed or counted in {@code pureKubernetesFileCount}
 * or {@code helmFileCount}.
 */
public class KubernetesParserStatistics {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesParserStatistics.class);
  private static final String COUNT_KUSTOMIZE_KEY = "iac.kustomize";
  private static final String COUNT_HELM_KEY = "iac.helm";
  private static final Version MIN_VERSION_WITH_TELEMETRY_SUPPORT = Version.create(10, 9);

  private int pureKubernetesFileCount;
  private int pureKubernetesParsedFileCount;
  private int helmFileCount;
  private int helmParsedFileCount;
  private int kustomizeFileCount;

  public <T> T recordFile(Supplier<T> o, @Nullable InputFileContext inputFileContext) {
    T result;
    if (inputFileContext instanceof HelmInputFileContext) {
      helmFileCount++;
      result = o.get();
      helmParsedFileCount++;
    } else {
      pureKubernetesFileCount++;
      result = o.get();
      pureKubernetesParsedFileCount++;
    }
    return result;
  }

  public void storeTelemetry(SensorContext sensorContext) {
    if (isKubernetesProject() && sensorContext.runtime().getApiVersion().isGreaterThanOrEqual(MIN_VERSION_WITH_TELEMETRY_SUPPORT)) {
      sensorContext.addTelemetryProperty(COUNT_HELM_KEY, helmFileCount == 0 ? "0" : "1");
      sensorContext.addTelemetryProperty(COUNT_KUSTOMIZE_KEY, kustomizeFileCount == 0 ? "0" : "1");
    }
  }

  public void logStatistics() {
    if (isKubernetesProject()) {
      LOG.debug("Kubernetes Parsing Statistics: Pure Kubernetes files count: {}, parsed: {}, not parsed: {}; Helm files count: {}, " +
        "parsed: {}, not parsed: {}; Kustomize file count: {}",
        pureKubernetesFileCount,
        pureKubernetesParsedFileCount,
        (pureKubernetesFileCount - pureKubernetesParsedFileCount),
        helmFileCount,
        helmParsedFileCount,
        (helmFileCount - helmParsedFileCount),
        kustomizeFileCount);
    }
  }

  private boolean isKubernetesProject() {
    return pureKubernetesFileCount != 0 || helmFileCount != 0;
  }

  public void recordKustomizeFile(InputFile inputFile) {
    var filename = inputFile.filename();
    if ("kustomization.yaml".equals(filename) || "kustomization.yml".equals(filename)) {
      kustomizeFileCount++;
    }
  }
}
