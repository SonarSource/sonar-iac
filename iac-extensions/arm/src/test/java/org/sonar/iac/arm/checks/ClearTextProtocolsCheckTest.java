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
package org.sonar.iac.arm.checks;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class ClearTextProtocolsCheckTest {

  IacCheck check = new ClearTextProtocolsCheck();

  @Test
  void testClearTextProtocolWithHttpsOnlyJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.Web_sites.json", check,
      issue(range(10, 8, 10, 26), "Make sure that using clear-text protocols is safe here."),
      issue(range(15, 14, 15, 35), "Omitting \"httpsOnly\" allows the use of clear-text protocols. Make sure it is safe here."));
  }

  @Test
  void testClearTextProtocolWithHttpsOnlyBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.Web_sites.bicep", check);
  }

  @Test
  void testClearTextProtocolWithHttpAllowedJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.Cdn_profiles_endpoints.json", check,
      issue(range(10, 8, 10, 29), "Make sure that using clear-text protocols is safe here."),
      issue(range(15, 14, 15, 48), "Omitting \"isHttpAllowed\" allows the use of clear-text protocols. Make sure it is safe here."));
  }

  @Test
  void testClearTextProtocolWithHttpAllowedBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.Cdn_profiles_endpoints.bicep", check);
  }

  @Test
  void testClearTextProtocolWithFtpsStateJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.Web_sites_config.json", check,
      issue(range(10, 8, 10, 33), "Make sure that using clear-text protocols is safe here."));
  }

  @Test
  void testClearTextProtocolWithFtpsStateBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.Web_sites_config.bicep", check);
  }

  @Test
  void testClearTextProtocolWithHttpsTrafficOnlyJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.Storage_storageAccounts.json", check,
      issue(range(10, 8, 10, 41), "Make sure that using clear-text protocols is safe here."));
  }

  @Test
  void testClearTextProtocolWithHttpsTrafficOnlyBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.Storage_storageAccounts.bicep", check);
  }

  @Test
  void testClearTextProtocolWithProtocolsContainingHttpsJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.ApiManagement_service_apis.json", check,
      issue(range(11, 10, 11, 16), "Make sure that using clear-text protocols is safe here."),
      issue(range(22, 10, 22, 16)));
  }

  @Test
  void testClearTextProtocolWithProtocolsContainingHttpsBicep() {
    BicepVerifier.verify("ClearTextProtocolsCheck/Microsoft.ApiManagement_service_apis.bicep", check);
  }

  @Test
  void testClearTextProtocolWithClientProtocolJson() {
    ArmVerifier.verify("ClearTextProtocolsCheck/Microsoft.Cache_redisEnterprise_databases.json", check,
      issue(range(10, 8, 10, 37), "Make sure that using clear-text protocols is safe here."));
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
      issue(range(10, 8, 10, 36), "Make sure that using clear-text protocols is safe here."));
  }

  @MethodSource("databaseTypeList")
  @ParameterizedTest(name = "[{index}] property sslEnforcement should be set to Enabled for type {0}")
  void testClearTextProtocolWithSslEnforcementInDifferentDatabasesBicep(String type) {
    String content = readTemplateAndReplace("ClearTextProtocolsCheck/Microsoft.DBforDbname_servers_template.bicep", type);
    BicepVerifier.verifyContent(content, check);
  }
}
