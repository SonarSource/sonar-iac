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
import org.sonar.iac.common.api.checks.IacCheck;

class DisabledLoggingCheckTest {

  final IacCheck check = new DisabledLoggingCheck();

  @Test
  void aws_S3_bucket_provider_version3() {
    TerraformVerifier.verifyWithProviderVersion("DisabledLoggingCheck/aws_S3_bucket_provider_version3.tf", check, "3");
  }

  @Test
  void aws_S3_bucket_provider_version4() {
    TerraformVerifier.verifyNoIssueWithProviderVersion("DisabledLoggingCheck/aws_S3_bucket_provider_version4.tf", check, "4");
  }

  @Test
  void aws_api_gateway_stage() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_api_gateway_stage.tf", check);
  }

  @Test
  void aws_apigatewayv2_stage() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_apigatewayv2_stage.tf", check);
  }

  @Test
  void aws_aws_msk_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_aws_msk_cluster.tf", check);
  }

  @Test
  void aws_neptune_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_neptune_cluster.tf", check);
  }

  @Test
  void aws_docdb_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_docdb_cluster.tf", check);
  }

  @Test
  void aws_mq_broker() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_mq_broker.tf", check);
  }

  @Test
  void aws_redshift_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_redshift_cluster.tf", check);
  }

  @Test
  void aws_global_accelerator() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_global_accelerator.tf", check);
  }

  @Test
  void aws_elastic_search_domain() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_elastic_search_domain.tf", check);
  }

  @Test
  void aws_cloudfront_distribution() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_cloudfront_distribution.tf", check);
  }

  @Test
  void aws_elastic_load_balancing() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_elastic_load_balancing.tf", check);
  }

  @Test
  void gcp_storage_bucket() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_storage_bucket.tf", check);
  }

  @Test
  void gcp_region_backend_service() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_region_backend_service.tf", check);
  }

  @Test
  void gcp_subnetwork() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_subnetwork.tf", check);
  }

  @Test
  void gcp_container_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_container_cluster.tf", check);
  }

  @Test
  void gcp_sql_database() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_sql_database.tf", check);
  }

  @Test
  void azurerm_function_app() {
    TerraformVerifier.verify("DisabledLoggingCheck/azurerm_function_app.tf", check);
  }

  @Test
  void azurerm_automation_runbook() {
    TerraformVerifier.verify("DisabledLoggingCheck/azurerm_automation_runbook.tf", check);
  }

  @Test
  void azurerm_app_service() {
    TerraformVerifier.verify("DisabledLoggingCheck/azurerm_app_service.tf", check);
  }

  @Test
  void azure_container_group() {
    TerraformVerifier.verify("DisabledLoggingCheck/azure_container_group.tf", check);
  }

  @Test
  void azure_storage_account() {
    TerraformVerifier.verify("DisabledLoggingCheck/azure_storage_account.tf", check);
  }
}
