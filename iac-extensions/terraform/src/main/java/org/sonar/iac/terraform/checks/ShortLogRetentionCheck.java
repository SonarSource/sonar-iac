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
package org.sonar.iac.terraform.checks;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.symbols.AttributeSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.lessThan;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.notEqualTo;

@Rule(key = "S6413")
public class ShortLogRetentionCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure that defining a short log retention duration is safe here.";
  private static final int DEFAULT = 30;
  private static final int MIN_DEFAULT = 14;

  @RuleProperty(
    key = "minimum_log_retention_days",
    defaultValue = "" + MIN_DEFAULT)
  int minimumLogRetentionDays = MIN_DEFAULT;

  @Override
  protected void registerResourceConsumer() {
    register(List.of("google_logging_project_bucket_config",
        "google_logging_billing_account_bucket_config",
        "google_logging_organization_bucket_config",
        "google_logging_folder_bucket_config"),
      resource -> {
        AttributeSymbol retention = resource.attribute("retention_days")
          .reportIf(isTooShortRetention(), MESSAGE);

        if (retention.isAbsent() && DEFAULT < minimumLogRetentionDays) {
          resource.report(MESSAGE);
        }
      });

    register(List.of("azurerm_mssql_server_extended_auditing_policy", "azurerm_mssql_database_extended_auditing_policy"),
      resource -> resource.attribute("retention_in_days")
        .reportIf(lessThanMinimumButNotZero(), MESSAGE));

    register("azurerm_app_service",
      resource -> {
        var logs = resource.block("logs");
        Set.of(logs.block("http_logs").block("azure_blob_storage"),
            logs.block("http_logs").block("file_system"),
            logs.block("application_logs").block("azure_blob_storage"))
          .forEach(block -> block.attribute("retention_in_days")
            .reportIf(lessThanMinimumButNotZero(), MESSAGE));
    });

    register("azurerm_firewall_policy",
      resource -> resource.block("insights").attribute("retention_in_days")
        .reportIf(lessThanMinimumButNotZero(), MESSAGE));

    register(Set.of("azurerm_monitor_log_profile", "azurerm_network_watcher_flow_log"),
      resource -> {
        var retentionPolicy = resource.block("retention_policy");
        var enabled = retentionPolicy.attribute("enabled");
        if (enabled.is(isFalse())) {
          enabled.report(MESSAGE);
          return;
        }
        retentionPolicy.attribute("days")
          .reportIf(lessThanMinimumButNotZero(), MESSAGE);
      });

    register(List.of("azurerm_sql_server", "azurerm_mysql_server", "azurerm_postgresql_server"),
      resource -> resource.block("threat_detection_policy").attribute("retention_days")
        .reportIf(lessThanMinimumButNotZero(), MESSAGE));
  }

  private Predicate<ExpressionTree> lessThanMinimumButNotZero() {
    return lessThan(minimumLogRetentionDays).and(notEqualTo("0"));
  }

  private Predicate<ExpressionTree> isTooShortRetention() {
    return expression -> TextUtils.getIntValue(expression)
      .map(retention -> retention == 0 ? DEFAULT : retention)
      .filter(retention -> retention < minimumLogRetentionDays)
      .isPresent();
  }
}
