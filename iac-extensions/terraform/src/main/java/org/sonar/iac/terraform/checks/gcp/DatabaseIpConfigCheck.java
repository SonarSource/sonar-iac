/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.terraform.checks.gcp;

import org.sonar.check.Rule;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

@Rule(key = "S6403")
public class DatabaseIpConfigCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure creating a GCP SQL instance without requiring TLS is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting %s allows unencrypted connections to the database. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("google_sql_database_instance",
      resource -> resource.block("settings")
        .reportIfAbsent(OMITTING_MESSAGE)
        .block("ip_configuration")
        .reportIfAbsent(OMITTING_MESSAGE)
        .attribute("require_ssl")
        .reportIfAbsent(OMITTING_MESSAGE)
        .reportIf(isFalse(), MESSAGE));
  }
}
