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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class ClearTextProtocolsCheckTest {

  IacCheck check = new ClearTextProtocolsCheck();

  @ParameterizedTest
  @CsvSource({"Microsoft.Web_sites.json,httpsOnly", "Microsoft.Cdn_profiles_endpoints.json,isHttpAllowed"})
  void testClearTextProtocolWithHttpsFlagJson(String fileName, String propertyName) {
    int endColumnForProperty = 17 + propertyName.length();
    int endColumnForType = 11 + fileName.length();
    ArmVerifier.verify("ClearTextProtocolsCheck/" + fileName, check,
      issue(range(9, 8, 9, endColumnForProperty), "Make sure that using clear-text protocols is safe here."),
      issue(range(14, 14, 14, endColumnForType), "Omitting \"" + propertyName + "\" allows the use of clear-text protocols. Make sure it is safe here."));
  }

  @Test
  void testClearTextProtocolWithHttpsFlagBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.Web_sites.bicep", check);
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.Cdn_profiles_endpoints.bicep", check);
  }

  @Test
  void testClearTextProtocolWithFtpsStateJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.Web_sites_config.json", check,
      issue(range(9, 8, 9, 33), "Make sure that using clear-text protocols is safe here."));
  }

  @Test
  void testClearTextProtocolWithFtpsStateBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.Web_sites_config.bicep", check);
  }

  @Test
  void testClearTextProtocolWithHttpsTrafficOnlyJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.Storage_storageAccounts.json", check,
      issue(range(9, 8, 9, 41), "Make sure that using clear-text protocols is safe here."));
  }

  @Test
  void testClearTextProtocolWithHttpsTrafficOnlyBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.Storage_storageAccounts.bicep", check);
  }

  @Test
  void testClearTextProtocolWithProtocolsContainingHttpsJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.ApiManagement_service_apis.json", check,
      issue(range(10, 10, 10, 16), "Make sure that using clear-text protocols is safe here."),
      issue(range(21, 10, 21, 16)));
  }

  @Test
  void testClearTextProtocolWithProtocolsContainingHttpsBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.ApiManagement_service_apis.bicep", check);
  }

  @Test
  void testClearTextProtocolWithClientProtocolJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.Cache_redisEnterprise_databases.json", check,
      issue(range(9, 8, 9, 37), "Make sure that using clear-text protocols is safe here."));
  }

  @Test
  void testClearTextProtocolWithClientProtocolBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.Cache_redisEnterprise_databases.bicep", check);
  }

  static Stream<String> databaseTypeList() {
    return Stream.of(
      "Microsoft.DBforMySQL/servers",
      "Microsoft.DBforMariaDB/servers",
      "Microsoft.DBforPostgreSQL/servers");
  }

  @MethodSource("databaseTypeList")
  @ParameterizedTest(name = "[{index}] property sslEnforcement should be set to Enabled for type {0}")
  void testClearTextProtocolWithSslEnforcementInDifferentDatabasesJson(String type) {
    String content = readTemplateAndReplace("ClearTextProtocolsCheck/Microsoft.DBforDbname_servers_template.json", type);
    ArmVerifier.verifyContent(content, check,
      issue(range(9, 8, 9, 36), "Make sure that using clear-text protocols is safe here."));
  }

  @MethodSource("databaseTypeList")
  @ParameterizedTest(name = "[{index}] property sslEnforcement should be set to Enabled for type {0}")
  void testClearTextProtocolWithSslEnforcementInDifferentDatabasesBicep(String type) {
    String content = readTemplateAndReplace("ClearTextProtocolsCheck/Microsoft.DBforDbname_servers_template.bicep", type);
    BicepVerifier.verifyContent(content, check);
  }
}
