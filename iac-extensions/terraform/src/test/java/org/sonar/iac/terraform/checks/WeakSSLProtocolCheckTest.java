/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;

class WeakSSLProtocolCheckTest {

  @Test
  void testAwsApiGatewayName() {
    TerraformVerifier.verify("WeakSSLProtocolCheck/aws_api_gateway_name.tf", new WeakSSLProtocolCheck());
  }

  @Test
  void testAwsApiGatewayv2Name() {
    TerraformVerifier.verify("WeakSSLProtocolCheck/aws_api_gatewayv2_name.tf", new WeakSSLProtocolCheck());
  }

  @Test
  void testAwsElasticsearchDomain() {
    TerraformVerifier.verify("WeakSSLProtocolCheck/aws_elasticsearch_domain.tf", new WeakSSLProtocolCheck());
  }

  @Test
  void testAzureMysqlServer() {
    TerraformVerifier.verify("WeakSSLProtocolCheck/azure_mysql_server.tf", new WeakSSLProtocolCheck());
  }

  @Test
  void testAzureMssqlServer() {
    TerraformVerifier.verify("WeakSSLProtocolCheck/azure_mssql_server.tf", new WeakSSLProtocolCheck());
  }

  @Test
  void testAzurePostgresqlServer() {
    TerraformVerifier.verify("WeakSSLProtocolCheck/azure_postgresql_server.tf", new WeakSSLProtocolCheck());
  }

  @Test
  void testAzureStorageAccount() {
    TerraformVerifier.verify("WeakSSLProtocolCheck/azure_storage_account.tf", new WeakSSLProtocolCheck());
  }

  @Test
  void testGcpComputeSslPolicy() {
    TerraformVerifier.verify("WeakSSLProtocolCheck/gcp_compute_ssl_policy.tf", new WeakSSLProtocolCheck());
  }
}
