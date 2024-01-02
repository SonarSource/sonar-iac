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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;

@Rule(key = "S6407")
public class AppEngineHandlerCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure creating a App Engine handler without requiring TLS is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting security_level allows unencrypted connections to the App Engine. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register(List.of("google_app_engine_standard_app_version", "google_app_engine_flexible_app_version"),
      resource -> resource.blocks("handlers")
        .forEach(handlers -> handlers.attribute("security_level")
          .reportIf(equalTo("SECURE_OPTIONAL").or(equalTo("SECURE_NEVER")).or(equalTo("SECURE_DEFAULT")), MESSAGE)
          .reportIfAbsent(OMITTING_MESSAGE)));
  }
}
