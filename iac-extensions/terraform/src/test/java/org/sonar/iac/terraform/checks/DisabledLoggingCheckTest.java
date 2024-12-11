/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.common.api.checks.IacCheck;

class DisabledLoggingCheckTest {

  final IacCheck check = new DisabledLoggingCheck();

  @Test
  void shouldValidateAwsS3BucketProviderVersion3() {
    TerraformVerifier.verifyWithProviderVersion("DisabledLoggingCheck/aws_S3_bucket_provider_version3.tf", check, "3");
  }

  @Test
  void shouldValidateAwsS3BucketNoProviderVersion() {
    // If version is not provided it should behave as version < 4
    TerraformVerifier.verify("DisabledLoggingCheck/aws_S3_bucket_provider_version3.tf", check);
  }

  @Test
  void shouldValidateAwsS3BucketProviderVersion4() {
    TerraformVerifier.verifyWithProviderVersion("DisabledLoggingCheck/aws_S3_bucket_provider_version4.tf", check, "4");
  }

  @Test
  void shouldValidateAwsS3BucketProviderVersion4NoBucketLogging() {
    TerraformVerifier.verifyWithProviderVersion("DisabledLoggingCheck/aws_S3_bucket_provider_version4_no_bucket_logging.tf", check, "4");
  }

  @Test
  void shouldValidateAwsApiGatewayStage() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_api_gateway_stage.tf", check);
  }

  @Test
  void shouldValidateAwsApiGatewayV2Stage() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_apigatewayv2_stage.tf", check);
  }

  @Test
  void aws_aws_msk_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_aws_msk_cluster.tf", check);
  }

  @Test
  void shouldValidateAwsNeptuneCluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_neptune_cluster.tf", check);
  }

  @Test
  void shouldValidateAwsDocdbCluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_docdb_cluster.tf", check);
  }

  @Test
  void shouldValidateAwsMqBroker() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_mq_broker.tf", check);
  }

  @Test
  void shouldValidateAwsRedshiftCluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_redshift_cluster.tf", check);
  }

  @Test
  void shouldValidateAwsGlobalAccelerator() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_global_accelerator.tf", check);
  }

  @Test
  void shouldValidateAwsElasticSearchDomain() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_elastic_search_domain.tf", check);
  }

  @Test
  void shouldValidateAwsCloudfrontDistribution() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_cloudfront_distribution.tf", check);
  }

  @Test
  void shouldValidateAwsElasticLoadBalancing() {
    TerraformVerifier.verify("DisabledLoggingCheck/aws_elastic_load_balancing.tf", check);
  }

  @Test
  void shouldValidateGcpStorageBucket() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_storage_bucket.tf", check);
  }

  @Test
  void shouldValidateGcpRegionBackendService() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_region_backend_service.tf", check);
  }

  @Test
  void shouldValidateGcpSubnetwork() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_subnetwork.tf", check);
  }

  @Test
  void shouldValidateGcpContainerCluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_container_cluster.tf", check);
  }

  @Test
  void shouldValidateGcpSqlDatabase() {
    TerraformVerifier.verify("DisabledLoggingCheck/gcp_sql_database.tf", check);
  }

  @Test
  void shouldValidateAzurermFunctionApp() {
    TerraformVerifier.verify("DisabledLoggingCheck/azurerm_function_app.tf", check);
  }

  @Test
  void shouldValidateAzurermAutomationRunbook() {
    TerraformVerifier.verify("DisabledLoggingCheck/azurerm_automation_runbook.tf", check);
  }

  @Test
  void shouldValidateAzurermAppService() {
    TerraformVerifier.verify("DisabledLoggingCheck/azurerm_app_service.tf", check);
  }

  @Test
  void shouldValidateAzureContainerGroup() {
    TerraformVerifier.verify("DisabledLoggingCheck/azure_container_group.tf", check);
  }
}
