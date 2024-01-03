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

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;

@Rule(key = "S6410")
public class LoadBalancerSslPolicyCheck extends AbstractNewResourceCheck {

  private static final String OMITTED_MESSAGE = "Set profile to disable support of weak cipher suites.";
  private static final String INVALID_VALUE_MESSAGE = "Change this code to disable support of weak cipher suites.";

  @Override
  protected void registerResourceConsumer() {
    register("google_compute_ssl_policy",
      resource -> resource.attribute("profile")
        .reportIfAbsent(OMITTED_MESSAGE)
        .reportIf(equalTo("COMPATIBLE").or(equalTo("MODERN")), INVALID_VALUE_MESSAGE));
  }

}
