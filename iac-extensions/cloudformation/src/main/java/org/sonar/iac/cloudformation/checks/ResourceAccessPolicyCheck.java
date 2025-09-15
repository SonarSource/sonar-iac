/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.cloudformation.checks;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.checks.utils.PolicyUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.checks.policy.PolicyValidator;

@Rule(key = "S6304")
public class ResourceAccessPolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure granting access to all resources is safe here.";
  private static final String EFFECT_MESSAGE = "Related effect";
  private static final String ACTION_MESSAGE = "Related action";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    // Related to SONARIAC-260 and https://docs.aws.amazon.com/kms/latest/developerguide/key-policies.html
    // The use of 'Resource = "*"' is always safe, so we can ignore the resource for this rule.
    if (resource.isType("AWS::KMS::Key")) {
      return;
    }
    PolicyUtils.getPolicies(resource.properties()).forEach(policy -> checkInsecurePolicy(ctx, policy));
  }

  private static void checkInsecurePolicy(CheckContext ctx, Policy policy) {
    PolicyValidator.findInsecureStatements(policy).forEach(statement -> ctx.reportIssue(statement.resource(), MESSAGE, List.of(
      new SecondaryLocation(statement.effect(), EFFECT_MESSAGE),
      new SecondaryLocation(statement.action(), ACTION_MESSAGE))));
  }
}
