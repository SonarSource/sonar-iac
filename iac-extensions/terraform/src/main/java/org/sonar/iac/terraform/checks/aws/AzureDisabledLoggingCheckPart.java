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
import org.sonar.iac.terraform.checks.DisabledLoggingCheck;
import org.sonar.iac.terraform.symbols.BlockSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

public class AzureDisabledLoggingCheckPart extends AbstractNewResourceCheck {

  private static final String APPLICATION_MESSAGE = "Make sure that deactivating application logs is safe here.";

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
          resource.report("Make sure that omitting the logs block is safe here.");
          return;
        }

        var httpLogs = logs.block("http_logs");
        var applicationLogs = logs.block("application_logs");
        if (httpLogs.isAbsent() && applicationLogs.isAbsent()) {
          logs.report("Make sure that omitting http and application logging blocks is safe here.");
          return;
        }

        boolean isHttpLogsDisabled = httpLogs.isAbsent();
        boolean isApplicationLogCompliant = checkApplicationLogs(applicationLogs, isHttpLogsDisabled);

        if (isHttpLogsDisabled) {
          if (isApplicationLogCompliant) {
            logs.report("Make sure that omitting HTTP logs is safe here. If impossible: Make sure that disabling logging is safe here.");
          } else {
            logs.report(DisabledLoggingCheck.MESSAGE);
          }
        }
      });
  }

  /**
   * Check 'application_logs' blog. The block is compliant if 'file_system_level' or 'azure_blob_storage' logging is enabled.
   * @return false if application log is not compliant at all
   */
  private static boolean checkApplicationLogs(BlockSymbol applicationLogs, boolean isHttpLogsDisabled) {
    var fileSystemLevel = applicationLogs.attribute("file_system_level");
    var azureBlobStorage = applicationLogs.block("azure_blob_storage");

    if (fileSystemLevel.isAbsent() &&  azureBlobStorage.isAbsent()) {
      if (!isHttpLogsDisabled) {
        fileSystemLevel.reportIfAbsent(APPLICATION_MESSAGE);
      }
      return false;
    }

    var blobStorageLevel = azureBlobStorage.attribute("level");
    boolean isFileSystemLogDisabled = fileSystemLevel.isAbsent() || fileSystemLevel.is(equalTo("Off"));
    boolean isBlobStorageLogDisabled = azureBlobStorage.isAbsent() || blobStorageLevel.is(equalTo("Off"));
    boolean isApplicationLogDisabled = isFileSystemLogDisabled && isBlobStorageLogDisabled;

    if (!isHttpLogsDisabled && isApplicationLogDisabled) {
      fileSystemLevel.reportIf(equalTo("Off"), APPLICATION_MESSAGE);
      blobStorageLevel.reportIf(equalTo("Off"), APPLICATION_MESSAGE);
    }
    return !isApplicationLogDisabled;
  }


}
