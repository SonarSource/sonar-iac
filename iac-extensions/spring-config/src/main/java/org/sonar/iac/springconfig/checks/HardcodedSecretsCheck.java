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
package org.sonar.iac.springconfig.checks;

import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.springconfig.tree.api.Tuple;

@Rule(key = "S6437")
public class HardcodedSecretsCheck extends AbstractSensitiveKeyCheck {
  private static final String MESSAGE = "Revoke and change this password, as it is compromised.";
  private static final Set<String> SENSITIVE_KEYS = Set.of(
    "spring.mail.password",
    "spring.sendgrid.api-key",
    "spring.cassandra.password",
    "spring.couchbase.password",
    "spring.data.mongodb.password",
    "spring.data.redis.password",
    "spring.data.redis.sentinel.password",
    "spring.datasource.hikari.password",
    "spring.datasource.password",
    "spring.datasource.tomcat.password",
    "spring.elasticsearch.password",
    "spring.h2.console.settings.web-admin-password",
    "spring.ldap.embedded.credential.password",
    "spring.ldap.password",
    "spring.neo4j.authentication.password",
    "spring.r2dbc.password",
    "spring.flyway.password",
    "spring.liquibase.password",
    "spring.sql.init.password",
    "spring.activemq.password",
    "spring.artemis.password",
    "spring.kafka.admin.ssl.key-password",
    "spring.kafka.admin.ssl.key-store-password",
    "spring.kafka.admin.ssl.trust-store-password",
    "spring.kafka.consumer.ssl.key-password",
    "spring.kafka.consumer.ssl.key-store-key",
    "spring.kafka.consumer.ssl.key-store-password",
    "spring.kafka.consumer.ssl.trust-store-password",
    "spring.kafka.producer.ssl.key-password",
    "spring.kafka.producer.ssl.key-store-key",
    "spring.kafka.producer.ssl.key-store-password",
    "spring.kafka.producer.ssl.trust-store-password",
    "spring.kafka.ssl.key-password",
    "spring.kafka.ssl.key-store-password",
    "spring.kafka.ssl.trust-store-password",
    "spring.kafka.streams.ssl.key-password",
    "spring.kafka.streams.ssl.key-store-key",
    "spring.kafka.streams.ssl.key-store-password",
    "spring.kafka.streams.ssl.trust-store-password",
    "spring.rabbitmq.password",
    "spring.rabbitmq.ssl.key-store-password",
    "spring.rabbitmq.ssl.trust-store-password",
    "spring.rabbitmq.stream.password",
    "server.ssl.key-password",
    "server.ssl.key-store-password",
    "server.ssl.trust-store-password",
    "spring.security.oauth2.resourceserver.opaquetoken.client-secret",
    "spring.security.user.password",
    "spring.rsocket.server.ssl.key-password",
    "spring.rsocket.server.ssl.key-store-password",
    "spring.rsocket.server.ssl.trust-store-password",
    "management.appoptics.metrics.export.api-token",
    "management.datadog.metrics.export.api-key",
    "management.datadog.metrics.export.application-key",
    "management.dynatrace.metrics.export.api-token",
    "management.elastic.metrics.export.api-key-credentials",
    "management.elastic.metrics.export.password",
    "management.humio.metrics.export.api-token",
    "management.influx.metrics.export.password",
    "management.influx.metrics.export.token",
    "management.kairos.metrics.export.password",
    "management.newrelic.metrics.export.api-key",
    "management.prometheus.metrics.export.pushgateway.password",
    "management.server.ssl.key-password",
    "management.server.ssl.key-store-password",
    "management.server.ssl.trust-store-password",
    "management.signalfx.metrics.export.access-token",
    "management.wavefront.api-token",
    "spring.devtools.remote.secret");
  private static final Pattern VARIABLE = Pattern.compile("\\$\\{[^}]+}");

  @Override
  protected Set<String> sensitiveKeys() {
    return SENSITIVE_KEYS;
  }

  @Override
  protected void checkValue(CheckContext ctx, Tuple tuple, String value) {
    if (isHardcoded(value)) {
      ctx.reportIssue(tuple.value(), MESSAGE);
    }
  }

  private static boolean isHardcoded(String value) {
    return !(value.isEmpty() || VARIABLE.matcher(value).find());
  }
}
