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

import org.junit.jupiter.api.Test;

class DisabledLoggingCheckTest {

  @Test
  void aws_S3_bucket() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_S3_bucket.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_api_gateway_stage() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_api_gateway_stage.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_api_gatewayv2_stage() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_api_gatewayv2_stage.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_aws_msk_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_aws_msk_cluster.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_neptune_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_neptune_cluster.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_docdb_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_docdb_cluster.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_mq_broker() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_mq_broker.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_redshift_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_redshift_cluster.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_global_accelerator() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_global_accelerator.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_elastic_search_domain() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_elastic_search_domain.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_cloudfront_distribution() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_cloudfront_distribution.tf", new DisabledLoggingCheck());
  }

  @Test
  void aws_elastic_load_balancing() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_elastic_load_balancing.tf", new DisabledLoggingCheck());
  }

  @Test
  void gcp_storage_bucket() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_storage_bucket.tf", new DisabledLoggingCheck());
  }

  @Test
  void gcp_region_backend_service() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_region_backend_service.tf", new DisabledLoggingCheck());
  }

  @Test
  void gcp_subnetwork() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_subnetwork.tf", new DisabledLoggingCheck());
  }

  @Test
  void gcp_container_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_container_cluster.tf", new DisabledLoggingCheck());
  }

  @Test
  void gcp_sql_database() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_sql_database.tf", new DisabledLoggingCheck());
  }

  @Test
  void azurerm_function_app() {
    TerraformVerifier.verify("DisabledLoggingCheck/azurerm_function_app.tf", new DisabledLoggingCheck());
  }

  @Test
  void azurerm_automation_runbook() {
    TerraformVerifier.verify("DisabledLoggingCheck/azurerm_automation_runbook.tf", new DisabledLoggingCheck());
  }
}
