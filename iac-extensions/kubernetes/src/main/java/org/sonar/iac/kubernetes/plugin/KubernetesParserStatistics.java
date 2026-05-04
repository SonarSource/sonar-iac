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
package org.sonar.iac.kubernetes.plugin;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

/**
 * Fields of this class:
 * <ul>
 *   <li>{@code pureKubernetesParsedFileCount} <= {@code pureKubernetesFileCount} files that are not part of a Helm project</li>
 *   <li>{@code helmParsedFileCount} <= {@code helmFileCount} files that are part of a Helm project</li>
 *   <li>{@code kustomizeFileCount} number of {@code kustomize.y[a]ml} encountered in a whole file system project.
 *   Because of missing identifiers, should normally be not included in other counts.</li>
 * </ul>
 */
public class KubernetesParserStatistics {
  private int pureKubernetesFileCount;
  private int pureKubernetesParsedFileCount;
  private int helmFileCount;
  private int helmParsedFileCount;
  private boolean helmInitializationFailed;

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

  public void storeTelemetry(SensorTelemetry sensorTelemetry) {
    if (pureKubernetesFileCount + helmFileCount == 0) {
      return;
    }
    sensorTelemetry.addNumericalMeasure("kubernetes.pure.files.count", pureKubernetesFileCount);
    sensorTelemetry.addNumericalMeasure("kubernetes.pure.files.parsed", pureKubernetesParsedFileCount);
    sensorTelemetry.addNumericalMeasure("kubernetes.helm.files.count", helmFileCount);
    sensorTelemetry.addNumericalMeasure("kubernetes.helm.files.parsed", helmParsedFileCount);
    sensorTelemetry.setBooleanMeasure("helm", helmFileCount > 0);
    if (helmInitializationFailed) {
      sensorTelemetry.setBooleanMeasure("helm.initialization.failed", true);
    }
  }

  public void setHelmInitializationFailed(boolean failed) {
    this.helmInitializationFailed = failed;
  }
}
