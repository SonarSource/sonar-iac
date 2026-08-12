/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks.gcp;

import org.sonar.check.Rule;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.BlockSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

@Rule(key = "S6403")
public class DatabaseIpConfigCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Change to disallow unencrypted connections.";
  private static final String OMITTING_MESSAGE = "Omitting \"%s\" allows unencrypted connections to the database.";
  private static final String ALLOW_UNENCRYPTED_AND_ENCRYPTED = "ALLOW_UNENCRYPTED_AND_ENCRYPTED";

  @Override
  protected void registerResourceConsumer() {
    register("google_sql_database_instance",
      resource -> resource.block("settings")
        .reportIfAbsent(String.format(OMITTING_MESSAGE, "settings.ip_configuration.ssl_mode"))
        .block("ip_configuration")
        .reportIfAbsent(String.format(OMITTING_MESSAGE, "ip_configuration.ssl_mode"))
        .consume(DatabaseIpConfigCheck::checkIpConfiguration));
  }

  private static void checkIpConfiguration(BlockSymbol ipConfiguration) {
    var sslMode = ipConfiguration.attribute("ssl_mode");
    var sslModeValue = sslMode.asString();
    if (sslModeValue != null) {
      // require_ssl is deprecated in favor of ssl_mode, which takes precedence over require_ssl when both are set:
      // https://docs.cloud.google.com/sql/docs/postgres/admin-api/rest/v1/instances#ipconfiguration
      if (ALLOW_UNENCRYPTED_AND_ENCRYPTED.equals(sslModeValue)) {
        sslMode.report(MESSAGE);
      }
      return;
    }
    ipConfiguration.attribute("require_ssl")
      .reportIfAbsent(String.format(OMITTING_MESSAGE, "ssl_mode"))
      .reportIf(isFalse(), MESSAGE);
  }
}
