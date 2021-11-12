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
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class DisabledLoggingCheckTest {

  @Test
  void test_s3_yaml() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_s3.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_s3_json() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_s3.json", new DisabledLoggingCheck(),
      new Verifier.Issue(range(5, 14, 5, 31), "Make sure that disabling logging is safe here."));
  }

  @Test
  void test_api_gateway_stage() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_api_gateway_stage.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_aws_msk_cluster() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_aws_msk_cluster.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_neptune_db_cluster() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_neptune_db_cluster.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_doc_db_cluster() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_doc_db_cluster.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_amazon_mq_broker() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_amazon_mq_broker.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_redshift_cluster() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_redshift_cluster.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_search_domain() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_search_domain.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_cloudfront_distribution() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_cloudfront_distribution.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_elastic_load_balancer() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_elastic_load_balancer.yaml", new DisabledLoggingCheck());
  }

  @Test
  void test_elastic_load_balancer_v2() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_elastic_load_balancer_v2.yaml", new DisabledLoggingCheck());
  }
}
