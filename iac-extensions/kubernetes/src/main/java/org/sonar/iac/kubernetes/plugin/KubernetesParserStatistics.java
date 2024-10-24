/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.kubernetes.plugin;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

/**
 * Fields of this class:
 * <ul>
 *   <li>{@code pureKubernetesParsedFileCount} <= {@code pureKubernetesFileCount} files that are not part of a Helm project</li>
 *   <li>{@code helmParsedFileCount} <= {@code helmFileCount} files that are part of a Helm project</li>
 *   <li>{@code kustomizePureKubernetesFileCount} <= {@code pureKubernetesFileCount} number of {@code kustomize.y[a]ml} encountered in a non-Helm project</li>
 *   <li>{@code kustomizeHelmFileCount} <= {@code helmFileCount} number of {@code kustomize.y[a]ml} encountered in a Helm project</li>
 * </ul>
 * Some files can be counted as both kustomization files and parsed Helm/K8S files, so these numbers don't necessarily need to add up.
 */
public class KubernetesParserStatistics {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesParserStatistics.class);
  private static final String COUNT_KUSTOMIZE_KEY = "cn_kustomize";
  private static final String COUNT_HELM_KEY = "cn_helm";
  private static final Version MIN_VERSION_WITH_TELEMETRY_SUPPORT = Version.create(10, 9);

  private int pureKubernetesFileCount;
  private int pureKubernetesParsedFileCount;
  private int helmFileCount;
  private int helmParsedFileCount;
  private int kustomizePureKubernetesFileCount;
  private int kustomizeHelmFileCount;

  public <T> T recordFile(Supplier<T> o, @Nullable InputFileContext inputFileContext) {
    T result;
    recordKustomizeFiles(inputFileContext);
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
      sensorContext.addTelemetryProperty(COUNT_KUSTOMIZE_KEY, kustomizePureKubernetesFileCount + kustomizeHelmFileCount == 0 ? "0" : "1");
    }
  }

  public void logStatistics() {
    if (isKubernetesProject()) {
      LOG.debug("Kubernetes Parsing Statistics: Pure Kubernetes files count: {}, parsed: {}, not parsed: {}; Helm files count: {}, " +
        "parsed: {}, not parsed: {}; Kustomize file count: pure Kubernetes {}, Helm: {}",
        pureKubernetesFileCount,
        pureKubernetesParsedFileCount,
        (pureKubernetesFileCount - pureKubernetesParsedFileCount),
        helmFileCount,
        helmParsedFileCount,
        (helmFileCount - helmParsedFileCount),
        kustomizePureKubernetesFileCount,
        kustomizeHelmFileCount);
    }
  }

  private boolean isKubernetesProject() {
    return pureKubernetesFileCount != 0 || helmFileCount != 0;
  }

  private void recordKustomizeFiles(@Nullable InputFileContext inputFileContext) {
    if (inputFileContext == null) {
      return;
    }
    String filename = inputFileContext.inputFile.filename();
    if ("kustomization.yaml".equals(filename) || "kustomization.yml".equals(filename)) {
      if (inputFileContext instanceof HelmInputFileContext) {
        kustomizeHelmFileCount++;
      } else {
        kustomizePureKubernetesFileCount++;
      }
    }
  }
}
