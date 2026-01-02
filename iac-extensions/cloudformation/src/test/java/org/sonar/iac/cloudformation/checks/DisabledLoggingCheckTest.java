/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;

import static org.sonar.iac.common.testing.Verifier.issue;

class DisabledLoggingCheckTest {

  private static final DisabledLoggingCheck CHECK = new DisabledLoggingCheck();

  @Test
  void shouldVerifyS3Yaml() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_s3.yaml", CHECK);
  }

  @Test
  void shouldVerifyS3Json() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_s3.json", CHECK,
      issue(5, 14, 5, 31,
        "Omitting \"LoggingConfiguration\" makes logs incomplete. Make sure it is safe here."),
      issue(18, 14, 18, 31),
      issue(46, 14, 46, 31),
      issue(107, 14, 107, 31));
  }

  @Test
  void shouldVerifyApiGatewayStage() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_api_gateway_stage.yaml", CHECK);
  }

  @Test
  void shouldVerifyAwsMskCluster() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_aws_msk_cluster.yaml", CHECK);
  }

  @Test
  void shouldVerifyNeptuneDbCluster() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_neptune_db_cluster.yaml", CHECK);
  }

  @Test
  void shouldVerifyDocDbCluster() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_doc_db_cluster.yaml", CHECK);
  }

  @Test
  void shouldVerifyAmazonMqBroker() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_amazon_mq_broker.yaml", CHECK);
  }

  @Test
  void shouldVerifyRedshiftCluster() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_redshift_cluster.yaml", CHECK);
  }

  @Test
  void shouldVerifySearchDomain() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_search_domain.yaml", CHECK);
  }

  @Test
  void shouldVerifyCloudfrontDistribution() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_cloudfront_distribution.yaml", CHECK);
  }

  @Test
  void shouldVerifyElasticLoadBalancer() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_elastic_load_balancer.yaml", CHECK);
  }

  @Test
  void shouldVerifyElasticLoadBalancerV2() {
    CloudformationVerifier.verify("DisabledLoggingCheck/test_elastic_load_balancer_v2.yaml", CHECK);
  }
}
