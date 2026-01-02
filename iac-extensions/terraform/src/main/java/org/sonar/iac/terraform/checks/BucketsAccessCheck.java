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
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;

import static java.lang.String.format;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.S3_BUCKET;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.matchesPattern;

@Rule(key = "S6265")
public class BucketsAccessCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure granting access to \"%s\" group is safe here.";
  private static final String SECONDARY_MSG = "Related bucket";

  @Override
  protected void registerResourceConsumer() {
    register(S3_BUCKET, resource -> resource.attribute("acl")
      .reportIf(equalTo("authenticated-read"), format(MESSAGE, "AuthenticatedUsers"), resource.toSecondary(SECONDARY_MSG))
      .reportIf(matchesPattern("public-read(-write)?"), format(MESSAGE, "AllUsers"), resource.toSecondary(SECONDARY_MSG)));
  }
}
