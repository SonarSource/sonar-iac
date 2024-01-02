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
