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
    if (jsonFileCount + bicepFileCount == 0) {
      return;
    }
    sensorTelemetry.addNumericalMeasure("azureresourcemanager.files.count", (long) jsonFileCount + bicepFileCount);
    sensorTelemetry.addNumericalMeasure("azureresourcemanager.files.json.count", jsonFileCount);
    sensorTelemetry.addNumericalMeasure("azureresourcemanager.files.bicep.count", bicepFileCount);
    sensorTelemetry.addNumericalMeasure("azureresourcemanager.files.json.parsed", jsonParsedFileCount);
    sensorTelemetry.addNumericalMeasure("azureresourcemanager.files.bicep.parsed", bicepParsedFileCount);
  }
}
