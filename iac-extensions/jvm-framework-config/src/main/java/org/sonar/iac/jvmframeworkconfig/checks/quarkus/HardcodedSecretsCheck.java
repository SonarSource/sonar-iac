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
package org.sonar.iac.jvmframeworkconfig.checks.quarkus;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.OptimizedListToPatternBuilder;
import org.sonar.iac.jvmframeworkconfig.checks.common.AbstractHardcodedSecrets;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

@Rule(key = "S6437")
public class HardcodedSecretsCheck extends AbstractHardcodedSecrets {
  private static final String NAMED_SEGMENT_PATTERN = "([\\w-]++\\.)++";

  // Sensitive keys and key patterns were scraped from https://quarkus.io/guides and its subpages.

  private static final Set<String> SENSITIVE_KEYS = Set.of(
    // Core Datasource
    "quarkus.datasource.password",
    "quarkus.flyway.password",
    "quarkus.liquibase.password",

    // Security & Auth
    "quarkus.keycloak.admin-client.client-secret",
    "quarkus.keycloak.admin-client.password",
    "quarkus.oauth2.client-secret",
    "quarkus.oidc-client.credentials.jwt.secret",
    "quarkus.oidc-client.credentials.secret",
    "quarkus.oidc.credentials.jwt.secret",
    "quarkus.oidc.credentials.secret",
    "quarkus.oidc.introspection-credentials.secret",
    "quarkus.security.ldap.dir-context.password",
    "quarkus.vault.client-token",

    // Infrastructure & DBs
    "quarkus.elasticsearch.api-key",
    "quarkus.elasticsearch.password",
    "quarkus.hibernate-search-orm.elasticsearch.password",
    "quarkus.hibernate-search-standalone.elasticsearch.password",
    "quarkus.infinispan-client.password",
    "quarkus.kafka-streams.ssl.key.password",
    "quarkus.kafka-streams.ssl.keystore.password",
    "quarkus.kafka-streams.ssl.truststore.password",
    "quarkus.kubernetes-client.password",
    "quarkus.kubernetes-client.token",
    "quarkus.mongodb.credentials.password",
    "quarkus.redis.password",

    // TLS, Certs & EventBus (Private Keys ONLY)
    "quarkus.grpc.server.ssl.key-store-password",
    "quarkus.tls.key-store.jks.password",
    "quarkus.tls.key-store.p12.password",
    "quarkus.tls.trust-store.jks.password",
    "quarkus.tls.trust-store.p12.password",
    "quarkus.vertx.eventbus.key-certificate-jks.password",
    "quarkus.vertx.eventbus.key-certificate-pfx.password",
    "quarkus.vertx.eventbus.trust-certificate-jks.password",
    "quarkus.vertx.eventbus.trust-certificate-pfx.password",

    // Utilities & Proxies
    "quarkus.container-image.password",
    "quarkus.mailer.password",
    "quarkus.observability.lgtm.password",
    "quarkus.proxy.password",
    "quarkus.spring-cloud-config.password");

  private static final Set<String> SENSITIVE_KEY_PATTERNS = Set.of(
    // Core Datasource
    "quarkus\\.datasource\\." + NAMED_SEGMENT_PATTERN + "password",
    "quarkus\\.flyway\\." + NAMED_SEGMENT_PATTERN + "password",
    "quarkus\\.liquibase\\." + NAMED_SEGMENT_PATTERN + "password",

    // OIDC Multi-Tenant & Named Clients (Compressed)
    "quarkus\\.oidc\\." + NAMED_SEGMENT_PATTERN + "secret",
    "quarkus\\.oidc-client\\." + NAMED_SEGMENT_PATTERN + "secret",

    // Infrastructure & DBs (Compressed)
    "quarkus\\.hibernate-search-orm\\." + NAMED_SEGMENT_PATTERN + "password",
    "quarkus\\.hibernate-search-standalone\\." + NAMED_SEGMENT_PATTERN + "password",
    "quarkus\\.infinispan-client\\." + NAMED_SEGMENT_PATTERN + "password",
    "quarkus\\.mongodb\\." + NAMED_SEGMENT_PATTERN + "connection-string",
    "quarkus\\.mongodb\\." + NAMED_SEGMENT_PATTERN + "password",
    "quarkus\\.redis\\." + NAMED_SEGMENT_PATTERN + "password",

    // TLS Registry & KeyStores (Compressed)
    "quarkus\\.tls\\." + NAMED_SEGMENT_PATTERN + "password",

    // Utilities, Networking & Observability (Compressed)
    "quarkus\\.grpc\\.clients\\." + NAMED_SEGMENT_PATTERN + "password",
    "quarkus\\.mailer\\." + NAMED_SEGMENT_PATTERN + "password",
    "quarkus\\.otel\\.exporter\\.otlp\\.([^.]++\\.)+proxy-options\\.password",
    "quarkus\\.proxy\\." + NAMED_SEGMENT_PATTERN + "password");

  @Override
  protected Set<String> sensitiveKeys() {
    return SENSITIVE_KEYS;
  }

  // Skip dev/test profile keys before regex evaluation — faster than a negative lookbehind
  @Override
  protected void checkTuple(CheckContext ctx, Tuple tuple) {
    var key = tuple.key().value().value();
    if (key.startsWith("%dev.") || key.startsWith("%test.")) {
      return;
    }
    super.checkTuple(ctx, tuple);
  }

  /*
   * The following sensitive key patterns also cover profile prefixes such as %prod.
   * More information on this topic here: https://quarkus.io/guides/config-reference#profiles
   */
  @Override
  protected Set<String> sensitiveKeyPatterns() {
    String profilePrefix = "(?:%[\\w,-]++\\.)?";

    String optimizedLiterals = OptimizedListToPatternBuilder.fromCollection(SENSITIVE_KEYS)
      .optimizeOnPrefix("quarkus.")
      .applyStringTransformation(s -> profilePrefix + s.replace(".", "\\."))
      .build();

    String optimizedPatterns = OptimizedListToPatternBuilder.fromCollection(SENSITIVE_KEY_PATTERNS)
      .optimizeOnPrefix("quarkus\\.")
      .applyStringTransformation(s -> profilePrefix + s)
      .build();

    return Set.of(optimizedLiterals, optimizedPatterns);
  }
}
