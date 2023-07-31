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
import org.sonar.iac.arm.ArmTestUtils;

import static org.sonar.iac.common.testing.Verifier.issue;

class AnonymousAccessToResourceCheckTest {
  AnonymousAccessToResourceCheck check = new AnonymousAccessToResourceCheck();

  @Test
  void shouldFindIssuesInWebSitesResourceJson() {
    ArmVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.Web_sites.json", check,
      issue(6, 14, 6, 35, "Omitting authsettingsV2 disables authentication. Make sure it is safe here."),
      issue(21, 14, 21, 44, "Make sure that disabling authentication is safe here."),
      issue(22, 14, 22, 61, "Make sure that disabling authentication is safe here."),
      issue(39, 14, 39, 44, "Make sure that disabling authentication is safe here."),
      issue(58, 14, 58, 61, "Make sure that disabling authentication is safe here."),
      issue(70, 10, 70, 40, "Make sure that disabling authentication is safe here."));
  }

  @MethodSource(value = "sensitiveDataFactoryTypes")
  @ParameterizedTest(name = "[{index}] JSON should check data factory secure access for type {0}")
  void shouldFindIssuesInDataFactoryJson(String type) {
    String content = ArmTestUtils.readTemplateAndReplace("AnonymousAccessToResourceCheck/Microsoft.DataFactory_factories_linkedservices.json", type);
    ArmVerifier.verifyContent(content, check,
      issue(12, 10, 12, 43, "Make sure that authorizing anonymous access is safe here."));
  }

  @Test
  void shouldFindIssuesInWebSitesResourceBicep() {
    BicepVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.Web_sites.bicep", check);
  }

  @Test
  void shouldFindIssuesInDataFactoryBicep() {
    BicepVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.DataFactory_factories_linkedservices.bicep", check);
  }

  private static Stream<String> sensitiveDataFactoryTypes() {
    return Stream.of("AzureBlobStorage",
      "FtpServer",
      "HBase",
      "Hive",
      "HttpServer",
      "Impala",
      "MongoDb",
      "OData",
      "Phoenix",
      "Presto",
      "RestService",
      "Spark",
      "Web");
  }
}
