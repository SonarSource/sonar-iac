/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.terraform.checks.aws;

import java.util.List;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

public class AzureDisabledLoggingCheckPart extends AbstractNewResourceCheck {

  @Override
  protected void registerResourceConsumer() {
    register(List.of("azurerm_function_app", "azurerm_function_app_slot"),
      resource -> resource.attribute("enable_builtin_logging")
        .reportIf(isFalse(), "Make sure that disabling built-in logging is safe here."));

    register("azurerm_automation_runbook",
      resource -> resource.attribute("log_progress")
        .reportIfAbsent("Make sure that omitting the activation of progress logging is safe here.")
        .reportIf(isFalse(), "Make sure that disabling progress logging is safe here."));

    register(List.of("azurerm_app_service", "azurerm_app_service_slot"),
      resource -> {
        var logs = resource.block("logs");
        if (logs.isAbsent()) {
          resource.report("Make sure that omitting the \"logs\" block is safe here.");
          return;
        }

        var httpLogs = logs.block("http_logs");
        var applicationLogs = logs.block("application_logs");
        boolean isHttpLogsDisabled = httpLogs.isAbsent();

        if (isHttpLogsDisabled && applicationLogs.isAbsent()) {
          logs.report("Make sure that omitting http and application logging blocks is safe here.");
          return;
        }

        // Check 'application_logs' blog. The block is compliant if 'file_system_level' or 'azure_blob_storage' logging is enabled.
        var fileSystemLevel = applicationLogs.attribute("file_system_level");
        var azureBlobStorage = applicationLogs.block("azure_blob_storage");

        boolean isFileSystemLogDisabled = fileSystemLevel.isAbsent() || fileSystemLevel.is(equalTo("Off"));
        boolean isBlobStorageLogDisabled = azureBlobStorage.isAbsent() || azureBlobStorage.attribute("level").is(equalTo("Off"));
        boolean isApplicationLogDisabled = isFileSystemLogDisabled && isBlobStorageLogDisabled;

        // Report on 'logs' or 'application_logs based on the 'http_logs' and 'application_logs' compliance
        if (isHttpLogsDisabled && isApplicationLogDisabled) {
          logs.report("Make sure that disabling logging is safe here.");
        } else if (isHttpLogsDisabled) {
          logs.report("Make sure that omitting HTTP logs is safe here.");
        } else if (isApplicationLogDisabled) {
          applicationLogs.report("Make sure that deactivating application logs is safe here.");
        }
      });
  }




}
