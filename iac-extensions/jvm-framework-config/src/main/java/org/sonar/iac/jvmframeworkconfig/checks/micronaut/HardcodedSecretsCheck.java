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
package org.sonar.iac.jvmframeworkconfig.checks.micronaut;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.jvmframeworkconfig.checks.common.AbstractHardcodedSecrets;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

import static org.sonar.iac.jvmframeworkconfig.tree.utils.JvmFrameworkConfigUtils.getStringValue;

@Rule(key = "S6437")
public class HardcodedSecretsCheck extends AbstractHardcodedSecrets {
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
  private static final List<String> SENSITIVE_KEYS_REGEX = List.of(
    "micronaut.http.services.[^.]++.proxy-password",
    "micronaut.http.services.[^.]++.ssl.key.password",
    "micronaut.http.services.[^.]++.ssl.key-store.password",
    "micronaut.http.services.[^.]++.ssl.trust-store.password",
    "micronaut.chatbots.telegram.bots.[^.]++.token",
    "flyway.datasources.[^.]++.password",
    "nats.[^.]++.password",
    "nats.[^.]++.token",
    "nats.[^.]++.tls.trust-store-password",
    "r2dbc.datasources.[^.]++.password",
    "rabbitmq.servers.[^.]++.password",
    "redis.servers.[^.]++.password",
    "micronaut.security.token.jwt.encryptions.secret.[^.]++.secret",
    "micronaut.security.token.jwt.signatures.secret.[^.]++.secret",
    "micronaut.security.ldap.[^.]++.context.manager-password",
    "micronaut.security.oauth2.clients.[^.]++.client-secret",
    "datasources.[^.]++.password");
  private static final Pattern PATTERN_PASSWORD_IN_CONNECTION_STRING = Pattern.compile("(?:;|^)AccountKey=(?<password>[a-zA-Z0-9+/=]{60,})(?:;|$)");
  private static final Pattern PATTERN_PASSWORD_IN_URL = Pattern.compile("(?<protocol>[a-zA-Z]++)://(?<username>[^:@]++):(?<password>[^@]++)@.++");
  private static final Map<String, Pattern> SENSITIVE_KEYS_WITH_PATTERN_VALUE = Map.of(
    "azure.credential.storage-shared-key.connection-string", PATTERN_PASSWORD_IN_CONNECTION_STRING,
    "micronaut.jms.activemq.artemis.connection-string", PATTERN_PASSWORD_IN_URL,
    "micronaut.jms.activemq.classic.connection-string", PATTERN_PASSWORD_IN_URL,
    "mqtt.client.server-uri", PATTERN_PASSWORD_IN_URL,
    "rabbitmq.uri", PATTERN_PASSWORD_IN_URL);
  // Extra use case: combine both wildcard in the key and URL with password in value
  private static final Pattern PATTERN_SENSITIVE_RABBITMQ_PROPERTY = Pattern.compile("rabbitmq.servers.[^.]++.uri");

  // Compiling patterns together is ~35% faster than checking patterns individually
  private static final Predicate<String> PREDICATE_SENSITIVE_KEY_FULL_REGEX = Pattern.compile(String.join("|", SENSITIVE_KEYS_REGEX)).asMatchPredicate();

  @Override
  protected Set<String> sensitiveKeys() {
    return SENSITIVE_KEYS;
  }

  @Override
  protected void checkTuple(CheckContext ctx, Tuple tuple) {
    var key = tuple.key().value().value();
    if (sensitiveKeys().contains(key) || PREDICATE_SENSITIVE_KEY_FULL_REGEX.test(key)) {
      var valueString = getStringValue(tuple);
      if (valueString != null) {
        checkValue(ctx, tuple, valueString);
      }
    } else if (SENSITIVE_KEYS_WITH_PATTERN_VALUE.containsKey(key)) {
      var pattern = SENSITIVE_KEYS_WITH_PATTERN_VALUE.get(key);
      checkValueWithPattern(ctx, pattern, tuple);
    } else {
      var matcher = PATTERN_SENSITIVE_RABBITMQ_PROPERTY.matcher(key);
      if (matcher.matches()) {
        checkValueWithPattern(ctx, PATTERN_PASSWORD_IN_URL, tuple);
      }
    }
  }

  private static void checkValueWithPattern(CheckContext ctx, Pattern pattern, Tuple tuple) {
    var valueString = getStringValue(tuple);
    if (valueString != null) {
      var matcher = pattern.matcher(valueString);
      if (matcher.find()) {
        ctx.reportIssue(computePasswordTextRange(matcher, tuple.value()), MESSAGE);
      }
    }
  }

  private static TextRange computePasswordTextRange(Matcher matcher, HasTextRange hasTextRange) {
    // If the value is split on multiple lines, we cannot recover the exact password location, so we just highlight the whole string
    if (hasTextRange.textRange().start().line() != hasTextRange.textRange().end().line()) {
      return hasTextRange.textRange();
    }
    int startPassword = matcher.start("password");
    int endPassword = matcher.end("password");
    int startLine = hasTextRange.textRange().start().line();
    int startLineOffset = hasTextRange.textRange().start().lineOffset() + startPassword;
    int endLine = hasTextRange.textRange().start().line();
    int endLineOffset = hasTextRange.textRange().start().lineOffset() + endPassword;
    return TextRanges.range(startLine, startLineOffset, endLine, endLineOffset);
  }
}
