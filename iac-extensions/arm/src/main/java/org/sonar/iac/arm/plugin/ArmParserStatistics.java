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
package org.sonar.iac.arm.plugin;

import javax.annotation.Nullable;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;

import static org.sonar.iac.arm.plugin.ArmSensor.isBicepFile;

public class ArmParserStatistics {
  private int jsonFileCount;
  private int jsonParsedFileCount;
  private int bicepFileCount;
  private int bicepParsedFileCount;

  public void recordFileStart(@Nullable InputFileContext inputFileContext) {
    if (inputFileContext != null && isBicepFile(inputFileContext)) {
      bicepFileCount++;
    } else {
      jsonFileCount++;
    }
  }

  public void recordFileEnd(@Nullable InputFileContext inputFileContext) {
    if (inputFileContext != null && isBicepFile(inputFileContext)) {
      bicepParsedFileCount++;
    } else {
      jsonParsedFileCount++;
    }
  }

  public void storeTelemetry(SensorTelemetry sensorTelemetry) {
    if (hasFiles()) {
      sensorTelemetry.addTelemetry("azureresourcemanager.files.count", String.valueOf(jsonFileCount + bicepFileCount));
      sensorTelemetry.addTelemetry("azureresourcemanager.files.json.count", String.valueOf(jsonFileCount));
      sensorTelemetry.addTelemetry("azureresourcemanager.files.bicep.count", String.valueOf(bicepFileCount));
      sensorTelemetry.addTelemetry("azureresourcemanager.files.json.parsed", String.valueOf(jsonParsedFileCount));
      sensorTelemetry.addTelemetry("azureresourcemanager.files.bicep.parsed", String.valueOf(bicepParsedFileCount));
    }
  }

  private boolean hasFiles() {
    return jsonFileCount != 0 || bicepFileCount != 0;
  }
}
