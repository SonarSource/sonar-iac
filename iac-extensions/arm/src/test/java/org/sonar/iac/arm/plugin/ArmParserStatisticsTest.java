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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.tests.ArmTelemetryReporter.storeTelemetryAndReport;
import static org.sonar.iac.arm.tests.ArmTestInputFileContextCreator.bicepFileContext;
import static org.sonar.iac.arm.tests.ArmTestInputFileContextCreator.jsonFileContext;

class ArmParserStatisticsTest {

  private final ArmParserStatistics statistics = new ArmParserStatistics();

  @Test
  void shouldNotReportTelemetryWhenNoFiles() {
    var telemetryProperties = storeTelemetryAndReport(statistics);

    assertThat(telemetryProperties).doesNotContainKey("iac.azureresourcemanager.files.count");
  }

  @Test
  void shouldRecordJsonFile() {
    statistics.recordFileStart(jsonFileContext());
    statistics.recordFileEnd(jsonFileContext());

    assertThat(storeTelemetryAndReport(statistics))
      .containsEntry("iac.azureresourcemanager.files.count", "1")
      .containsEntry("iac.azureresourcemanager.files.json.count", "1")
      .containsEntry("iac.azureresourcemanager.files.json.parsed", "1")
      .containsEntry("iac.azureresourcemanager.files.bicep.count", "0")
      .containsEntry("iac.azureresourcemanager.files.bicep.parsed", "0");
  }

  @Test
  void shouldRecordBicepFile() {
    statistics.recordFileStart(bicepFileContext());
    statistics.recordFileEnd(bicepFileContext());

    assertThat(storeTelemetryAndReport(statistics))
      .containsEntry("iac.azureresourcemanager.files.count", "1")
      .containsEntry("iac.azureresourcemanager.files.json.count", "0")
      .containsEntry("iac.azureresourcemanager.files.json.parsed", "0")
      .containsEntry("iac.azureresourcemanager.files.bicep.count", "1")
      .containsEntry("iac.azureresourcemanager.files.bicep.parsed", "1");
  }

  @Test
  void shouldCountTotalFiles() {
    statistics.recordFileStart(jsonFileContext());
    statistics.recordFileStart(bicepFileContext());
    statistics.recordFileStart(jsonFileContext());

    assertThat(storeTelemetryAndReport(statistics))
      .containsEntry("iac.azureresourcemanager.files.count", "3")
      .containsEntry("iac.azureresourcemanager.files.json.count", "2")
      .containsEntry("iac.azureresourcemanager.files.bicep.count", "1");
  }
}
