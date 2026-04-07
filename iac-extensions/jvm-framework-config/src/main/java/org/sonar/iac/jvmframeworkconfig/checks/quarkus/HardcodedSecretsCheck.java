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
  private static final String QUARKUS_DATASOURCE = "quarkus\\.datasource\\.";

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
    QUARKUS_DATASOURCE + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,
    "quarkus\\.flyway\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,
    "quarkus\\.liquibase\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,

    // OIDC Multi-Tenant & Named Clients (Compressed)
    "quarkus\\.oidc\\." + NAMED_SEGMENT_PATTERN + "secret",
    "quarkus\\.oidc-client\\." + NAMED_SEGMENT_PATTERN + "secret",

    // Infrastructure & DBs (Compressed)
    "quarkus\\.hibernate-search-orm\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,
    "quarkus\\.hibernate-search-standalone\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,
    "quarkus\\.infinispan-client\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,
    "quarkus\\.mongodb\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,
    "quarkus\\.redis\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,

    // TLS Registry & KeyStores (Compressed)
    "quarkus\\.tls\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,

    // Utilities, Networking & Observability (Compressed)
    "quarkus\\.grpc\\.clients\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,
    "quarkus\\.mailer\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP,
    "quarkus\\.otel\\.exporter\\.otlp\\." + NAMED_SEGMENT_PATTERN_NP + "proxy-options\\.password",
    "quarkus\\.proxy\\." + NAMED_SEGMENT_PATTERN + PASSWORD_GROUP);

  private static final Map<String, Pattern> SENSITIVE_KEYS_WITH_PATTERN_VALUE = Map.of(
    "quarkus.datasource.reactive.url", PATTERN_PASSWORD_IN_URL,
    "quarkus.security.ldap.dir-context.url", PATTERN_PASSWORD_IN_URL,
    "quarkus.mongodb.connection-string", PATTERN_PASSWORD_IN_URL,
    "quarkus.rest-client.url", PATTERN_PASSWORD_IN_URL,
    "quarkus.rest-client.uri", PATTERN_PASSWORD_IN_URL,
    "quarkus.smallrye-graphql-client.url", PATTERN_PASSWORD_IN_URL);
  private static final Pattern NAMED_JDBC_URL_PATTERN = Pattern.compile(QUARKUS_DATASOURCE + NAMED_SEGMENT_PATTERN_NP + "jdbc\\.url");
  private static final Map<Pattern, Pattern> SENSITIVE_KEY_PATTERNS_WITH_PATTERN_VALUE = Map.of(
    Pattern.compile(QUARKUS_DATASOURCE + NAMED_SEGMENT_PATTERN_NP + "reactive\\.url"), PATTERN_PASSWORD_IN_URL,
    Pattern.compile("quarkus\\.mongodb\\." + NAMED_SEGMENT_PATTERN + "connection-string"), PATTERN_PASSWORD_IN_URL,
    Pattern.compile("quarkus\\.rest-client\\." + NAMED_SEGMENT_PATTERN + "url"), PATTERN_PASSWORD_IN_URL,
    Pattern.compile("quarkus\\.rest-client\\." + NAMED_SEGMENT_PATTERN + "uri"), PATTERN_PASSWORD_IN_URL,
    Pattern.compile("quarkus\\.smallrye-graphql-client\\." + NAMED_SEGMENT_PATTERN + "url"), PATTERN_PASSWORD_IN_URL);

  @Override
  protected Set<String> sensitiveKeys() {
    return SENSITIVE_KEYS;
  }

  // Skip dev/test profile keys before regex evaluation
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

  @Override
  protected void checkTupleWithAdditionalPatterns(CheckContext ctx, Tuple tuple) {
    var key = tuple.key().value().value();

    // JDBC URLs support both embedded credentials (user:pass@host) and query-param (?password=value) formats
    if ("quarkus.datasource.jdbc.url".equals(key) || NAMED_JDBC_URL_PATTERN.matcher(key).matches()) {
      if (!checkValueWithPattern(ctx, PATTERN_PASSWORD_IN_URL, tuple)) {
        checkValueWithPattern(ctx, PATTERN_PASSWORD_IN_JDBC_URL, tuple);
      }
      return;
    }

    var pattern = SENSITIVE_KEYS_WITH_PATTERN_VALUE.get(key);
    if (pattern != null) {
      checkValueWithPattern(ctx, pattern, tuple);
      return;
    }

    SENSITIVE_KEY_PATTERNS_WITH_PATTERN_VALUE.entrySet().stream()
      .filter(e -> e.getKey().matcher(key).matches())
      .findFirst()
      .ifPresent(e -> checkValueWithPattern(ctx, e.getValue(), tuple));
  }
}
