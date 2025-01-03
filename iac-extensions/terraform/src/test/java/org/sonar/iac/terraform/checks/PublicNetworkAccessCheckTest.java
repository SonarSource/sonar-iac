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

class PublicNetworkAccessCheckTest {

  @Test
  void aws() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/aws.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void azureMachineLearningWorkspace() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/azureMachineLearningWorkspace.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void azurePublicNetworkAccess() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/azurePublicNetworkAccess.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void azurePublicNetworkVariant() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/azurePublicNetworkVariant.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void azurePublicIpRelatedParameters() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/azurePublicIpRelatedParameters.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void azureDatabaseEndpoint() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/azureDatabaseEndpoint.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void azureKubernetesCluster() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/azureKubernetesCluster.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void gcp_cloudbuild_worker_pool() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/gcp_cloudbuild_worker_pool.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void gcp_compute_instance() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/gcp_compute_instance.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void gcp_notebooks_instance() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/gcp_notebooks_instance.tf", new PublicNetworkAccessCheck());
  }

  @Test
  void gcp_sql_database_instance() {
    TerraformVerifier.verify("PublicNetworkAccessCheck/gcp_sql_database_instance.tf", new PublicNetworkAccessCheck());
  }
}
