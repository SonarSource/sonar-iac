/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

class ClearTextProtocolsCheckTest {

  @Test
  void aws_msk() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/aws_msk.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void aws_elasticsearch_domain() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/aws_elasticsearch_domain.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void aws_opensearch_domain() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/aws_opensearch_domain.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void aws_lb_listener() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/aws_lb_listener.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void aws_elasticsearch_replication_group() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/aws_elasticache_replication_group.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void aws_ecs_task_definition() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/aws_ecs_task_definition.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void aws_kinesis_stream() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/aws_kinesis_stream.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void azurerm_spring_cloud_app() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/azurerm_spring_cloud_app.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void azurerm_function_app() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/azurerm_function_app.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void azurerm_app_service() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/azurerm_app_service.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void azurerm_cdn_endpoint() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/azurerm_cdn_endpoint.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void azurerm_redis_enterprise_database() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/azurerm_redis_enterprise_database.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void azurerm_xyzsql_server() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/azurerm_xyzsql_server.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void azurerm_api_management_api() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/azurerm_api_management_api.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void azurerm_storage_account() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/azurerm_storage_account.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void google_compute_region_backend_service() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/google_compute_region_backend_service.tf", new ClearTextProtocolsCheck());
  }
}
