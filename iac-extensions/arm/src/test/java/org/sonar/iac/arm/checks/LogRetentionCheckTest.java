/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.arm.checks;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.arm.checks.ArmVerifier.verifyContent;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.FileReaderUtils.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class LogRetentionCheckTest {

  LogRetentionCheck check = new LogRetentionCheck();

  @Test
  void testLogRetentionFireWallPoliciesJson() {
    verify("LogRetentionCheck/Microsoft.Network_firewallPolicies.json", check,
      issue(range(12, 10, 12, 28), "Make sure that defining a short log retention duration is safe here."),
      issue(range(21, 20, 23, 9), "Omitting \"retentionDays\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(31, 20, 33, 9), "Omitting \"isEnabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(42, 10, 42, 28), "Disabling \"isEnabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(43, 10, 43, 28), "Make sure that defining a short log retention duration is safe here."),
      issue(range(53, 10, 53, 28), "Disabling \"isEnabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(60, 14, 60, 50), "Omitting \"insights\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(77, 14, 77, 32)));
  }

  @Test
  void testLogRetentionFireWallPoliciesBicep() {
    BicepVerifier.verify("LogRetentionCheck/Microsoft.Network_firewallPolicies.bicep", check);
  }

  @Test
  void testLogRetentionFireWallPoliciesWithCustomValueJson() {
    check.retentionPeriodInDays = 30;
    verify("LogRetentionCheck/Microsoft.Network_firewallPolicies_custom_value.json", check,
      issue(range(12, 10, 12, 29), "Make sure that defining a short log retention duration is safe here."));
  }

  @Test
  void testLogRetentionFireWallPoliciesWithCustomValueBicep() {
    check.retentionPeriodInDays = 30;
    BicepVerifier.verify("LogRetentionCheck/Microsoft.Network_firewallPolicies_custom_value.bicep", check);
  }

  @Test
  void testLogRetentionFlowLogsJson() {
    verify("LogRetentionCheck/Microsoft.Network_networkWatchers_flowLogs.json", check,
      issue(range(11, 10, 11, 19), "Make sure that defining a short log retention duration is safe here."),
      issue(range(21, 27, 23, 9), "Omitting \"days\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(31, 27, 33, 9), "Omitting \"enabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(42, 10, 42, 19), "Make sure that defining a short log retention duration is safe here."),
      issue(range(43, 10, 43, 26), "Disabling \"enabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(54, 10, 54, 26), "Disabling \"enabled\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(60, 14, 60, 58), "Omitting \"retentionPolicy\" results in a short log retention duration. Make sure it is safe here."),
      issue(range(77, 14, 77, 23)));
  }

  @Test
  void testLogRetentionFlowLogsBicep() {
    BicepVerifier.verify("LogRetentionCheck/Microsoft.Network_networkWatchers_flowLogs.bicep", check);
  }

  static Stream<String> logRetentionAsSimpleProperty() {
    return Stream.of(
      "Microsoft.Sql/servers/auditingSettings",
      "Microsoft.Sql/servers/databases/securityAlertPolicies",
      "Microsoft.Sql/servers/auditingPolicies",
      "Microsoft.Synapse/workspaces/auditingSettings",
      "Microsoft.Synapse/workspaces/sqlPools/securityAlertPolicies",
      "Microsoft.DBforMariaDB/servers/securityAlertPolicies");
  }

  @MethodSource("logRetentionAsSimpleProperty")
  @ParameterizedTest(name = "[{index}] JSON should check log retention duration for type {0}")
  void shouldCheckLogRetentionAsSimpleProperty(String type) {
    String content = readTemplateAndReplace("LogRetentionCheck/simpleRetentionDaysProperty_template.json", type);
    int endColumnForType = 16 + type.length();
    verifyContent(content, check,
      issue(9, 8, 9, 26, "Make sure that defining a short log retention duration is safe here."),
      issue(13, 14, 13, endColumnForType, "Omitting \"retentionDays\" results in a short log retention duration. Make sure it is safe here."));
  }

  @MethodSource("logRetentionAsSimpleProperty")
  @ParameterizedTest(name = "[{index}] Bicep should check log retention duration for type {0}")
  void shouldCheckLogRetentionAsSimplePropertyBicep(String type) {
    String content = readTemplateAndReplace("LogRetentionCheck/simpleRetentionDaysProperty_template.bicep", type);
    int endColumnForType = 16 + type.length();
    BicepVerifier.verifyContent(content, check,
      issue(4, 4, 4, 20, "Make sure that defining a short log retention duration is safe here."),
      issue(8, 16, 8, endColumnForType, "Omitting \"retentionDays\" results in a short log retention duration. Make sure it is safe here."));
  }
}
