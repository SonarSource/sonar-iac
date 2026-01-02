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
