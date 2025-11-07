/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

import static org.sonar.iac.terraform.checks.TerraformVerifier.verify;

class ShortLogRetentionCheckTest {

  @Test
  void shouldCheckGcpLogging() {
    verify("ShortLogRetentionCheck/gcp_logging.tf", new ShortLogRetentionCheck());
  }

  @Test
  void shouldCheckGcpLoggingCustom() {
    ShortLogRetentionCheck check = new ShortLogRetentionCheck();
    check.minimumLogRetentionDays = 300;
    verify("ShortLogRetentionCheck/gcp_logging_custom.tf", check);
  }

  @Test
  void shouldCheckAzureMssqlAuditingPolicy() {
    verify("ShortLogRetentionCheck/azure_mssql_auditing_policy.tf", new ShortLogRetentionCheck());
  }

  @Test
  void shouldCheckAzureAppService() {
    verify("ShortLogRetentionCheck/azure_app_service.tf", new ShortLogRetentionCheck());
  }

  @Test
  void shouldCheckAzureFirewallPolicy() {
    verify("ShortLogRetentionCheck/azure_firewall_policy.tf", new ShortLogRetentionCheck());
  }

  @Test
  void shouldCheckAzureMonitorLogProfile() {
    verify("ShortLogRetentionCheck/azure_monitor_log_profile.tf", new ShortLogRetentionCheck());
  }

  @Test
  void shouldCheckAzureMysqlServer() {
    verify("ShortLogRetentionCheck/azure_mysql_server.tf", new ShortLogRetentionCheck());
  }

  @Test
  void shouldCheckAwsCloudwatch() {
    verify("ShortLogRetentionCheck/aws_cloudwatch_log_group.tf", new ShortLogRetentionCheck());
  }
}
