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
package org.sonar.iac.jvmframeworkconfig.checks.micronaut;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.OptimizedListToPatternBuilder;
import org.sonar.iac.jvmframeworkconfig.checks.common.AbstractHardcodedSecrets;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

@Rule(key = "S6437")
public class HardcodedSecretsCheck extends AbstractHardcodedSecrets {
  // Micronaut named config segments are not confirmed to support nesting, so single-segment patterns are used here
  // instead of the multi-segment variants defined in AbstractHardcodedSecrets (see there for more details).
  private static final String SINGLE_NAMED_SEGMENT_PATTERN = "[\\w-]++\\.";
  private static final String SINGLE_NAMED_SEGMENT_PATTERN_NP = "[\\w-]+\\.";
  private static final String MICRONAUT_HTTP_SERVICES = "micronaut\\.http\\.services\\.";
  private static final String NATS = "nats\\.";

  private static final Set<String> SENSITIVE_KEYS = Set.of(
    "micronaut.ssl.key.password",
    "micronaut.ssl.key-store.password",
    "micronaut.ssl.trust-store.password",
    "micronaut.server.ssl.key.password",
    "micronaut.server.ssl.key-store.password",
    "micronaut.server.ssl.trust-store.password",
    "micronaut.http.client.proxy-password",
    "micronaut.http.client.ssl.key.password",
    "micronaut.http.client.ssl.key-store.password",
    "micronaut.http.client.ssl.trust-store.password",
    "acme.account-key",
    "aws.client.proxy-password",
    "aws.netty-client.proxy.password",
    "aws.apache-client.proxy.password",
    "aws.service-discovery.client.proxy-password",
    "aws.secretKey",
    "aws.sessionToken",
    "azure.cosmos.credential",
    "azure.credential.client-secret.secret",
    "azure.credential.client-certificate.pfx-certificate-password",
    "azure.credential.storage-shared-key.account-key",
    "azure.credential.username-password.password",
    "eureka.client.proxy-password",
    "vault.client.proxy-password",
    "spring.cloud.config.proxy-password",
    "spring.cloud.config.password",
    "consul.client.proxy-username",
    "consul.client.asl-token",
    "infinispan.client.hotrod.security.authentication.password",
    "infinispan.client.hotrod.security.ssl.key-store-password",
    "infinispan.client.hotrod.security.ssl.trust-store-password",
    "javamail.authentication.password",
    "mailjet.api-secret",
    "postmark.api-token",
    "sendgrid.api-key",
    "gcp.credentials.encoded-key",
    "micronaut.jms.activemq.artemis.password",
    "micronaut.jms.activemq.classic.password",
    "mongodb.credential",
    "mongodb.apply-connection-string",
    "mongodb.connection-pool.apply-connection-string",
    "mongodb.cluster.apply-connection-string",
    "mongodb.server.apply-connection-string",
    "mongodb.socket.apply-connection-string",
    "mongodb.ssl.apply-connection-string",
    "mqtt.client.password",
    "mqtt.client.ssl.password",
    "neo4j.password",
    "oci.pass-phrase",
    "oci.passphrase",
    "oci.private-key",
    "micronaut.http.client.micronaut.http.oci-oke.client.proxy-password",
    "oci.config.oke-workload-identity.token",
    "pulsar.authentication-jwt",
    "pulsar.tls-trust-store-password",
    "rabbitmq.password",
    "redis.password",
    "micronaut.security.token.jwt.generator.refresh-token.secret",
    "micronaut.security.token.jwt.claims-validators.openid-idtoken",
    "jasync.client.password",
    "vertx.mysql.client.password",
    "vertx.pg.client.password",
    "tracing.zipkin.http.proxy-password",
    "tracing.jaeger.sender.auth-password",
    "otel.exporter.zipkin.proxy-password");
  private static final Set<String> SENSITIVE_KEY_PATTERNS = Set.of(
    MICRONAUT_HTTP_SERVICES + SINGLE_NAMED_SEGMENT_PATTERN_NP + "proxy-password",
    MICRONAUT_HTTP_SERVICES + SINGLE_NAMED_SEGMENT_PATTERN_NP + "ssl\\.key\\.password",
    MICRONAUT_HTTP_SERVICES + SINGLE_NAMED_SEGMENT_PATTERN_NP + "ssl\\.key-store\\.password",
    MICRONAUT_HTTP_SERVICES + SINGLE_NAMED_SEGMENT_PATTERN_NP + "ssl\\.trust-store\\.password",
    "micronaut\\.chatbots\\.telegram\\.bots\\." + SINGLE_NAMED_SEGMENT_PATTERN + "token",
    "flyway\\.datasources\\." + SINGLE_NAMED_SEGMENT_PATTERN + "password",
    NATS + SINGLE_NAMED_SEGMENT_PATTERN + "password",
    NATS + SINGLE_NAMED_SEGMENT_PATTERN + "token",
    NATS + SINGLE_NAMED_SEGMENT_PATTERN_NP + "tls\\.trust-store-password",
    "r2dbc\\.datasources\\." + SINGLE_NAMED_SEGMENT_PATTERN + "password",
    "rabbitmq\\.servers\\." + SINGLE_NAMED_SEGMENT_PATTERN + "password",
    "redis\\.servers\\." + SINGLE_NAMED_SEGMENT_PATTERN + "password",
    "micronaut\\.security\\.token\\.jwt\\.encryptions\\.secret\\." + SINGLE_NAMED_SEGMENT_PATTERN + "secret",
    "micronaut\\.security\\.token\\.jwt\\.signatures\\.secret\\." + SINGLE_NAMED_SEGMENT_PATTERN + "secret",
    "micronaut\\.security\\.ldap\\." + SINGLE_NAMED_SEGMENT_PATTERN_NP + "context\\.manager-password",
    "micronaut\\.security\\.oauth2\\.clients\\." + SINGLE_NAMED_SEGMENT_PATTERN_NP + "client-secret",
    "datasources\\." + SINGLE_NAMED_SEGMENT_PATTERN + "password");
  // Intentionally letters-only scheme: Micronaut broker URIs (amqp://, mqtt://, tcp://) never use
  // compound schemes like jdbc:postgresql://, which are handled by the base-class PATTERN_PASSWORD_IN_URL.
  private static final Pattern PATTERN_PASSWORD_IN_SIMPLE_URL = Pattern.compile("[a-zA-Z]++://(?<username>[^:@]++):(?<password>[^@]++)@.++");
  private static final Map<String, Pattern> SENSITIVE_KEYS_WITH_PATTERN_VALUE = Map.of(
    "azure.credential.storage-shared-key.connection-string", PATTERN_PASSWORD_IN_CONNECTION_STRING,
    "micronaut.jms.activemq.artemis.connection-string", PATTERN_PASSWORD_IN_SIMPLE_URL,
    "micronaut.jms.activemq.classic.connection-string", PATTERN_PASSWORD_IN_SIMPLE_URL,
    "mqtt.client.server-uri", PATTERN_PASSWORD_IN_SIMPLE_URL,
    "rabbitmq.uri", PATTERN_PASSWORD_IN_SIMPLE_URL);
  // Extra use case: combine both wildcard in the key and URL with password in value
  private static final Pattern PATTERN_SENSITIVE_RABBITMQ_PROPERTY = Pattern.compile("rabbitmq.servers.[^.]++.uri");

  @Override
  protected Set<String> sensitiveKeys() {
    return SENSITIVE_KEYS;
  }

  @Override
  protected Set<String> sensitiveKeyPatterns() {
    return Set.of(OptimizedListToPatternBuilder
      .fromCollection(SENSITIVE_KEY_PATTERNS)
      .optimizeOnPrefix("micronaut\\.")
      .build());
  }

  @Override
  protected void checkTupleWithAdditionalPatterns(CheckContext ctx, Tuple tuple) {
    var key = tuple.key().value().value();
    var pattern = SENSITIVE_KEYS_WITH_PATTERN_VALUE.get(key);
    if (pattern != null) {
      checkValueWithPattern(ctx, pattern, tuple);
      return;
    }

    var matcher = PATTERN_SENSITIVE_RABBITMQ_PROPERTY.matcher(key);
    if (matcher.matches()) {
      checkValueWithPattern(ctx, PATTERN_PASSWORD_IN_SIMPLE_URL, tuple);
    }
  }

}
