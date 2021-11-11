/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
  void test_S3_bucket() {
    TerraformVerifier.verify("DisabledLoggingCheck/test_S3_bucket.tf", new DisabledLoggingCheck());
  }

  @Test
  void test_api_gateway_stage() {
    TerraformVerifier.verify("DisabledLoggingCheck/test_api_gateway_stage.tf", new DisabledLoggingCheck());
  }

  @Test
  void test_api_gatewayv2_stage() {
    TerraformVerifier.verify("DisabledLoggingCheck/test_api_gatewayv2_stage.tf", new DisabledLoggingCheck());
  }

  @Test
  void test_aws_msk_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/test_aws_msk_cluster.tf", new DisabledLoggingCheck());
  }

  @Test
  void test_neptune_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/test_neptune_cluster.tf", new DisabledLoggingCheck());
  }

  @Test
  void test_docdb_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/test_docdb_cluster.tf", new DisabledLoggingCheck());
  }

  @Test
  void test_mq_broker() {
    TerraformVerifier.verify("DisabledLoggingCheck/test_mq_broker.tf", new DisabledLoggingCheck());
  }

  @Test
  void test_redshift_cluster() {
    TerraformVerifier.verify("DisabledLoggingCheck/test_redshift_cluster.tf", new DisabledLoggingCheck());
  }

}
