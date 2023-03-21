/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.terraform.checks;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.checks.policy.PolicyValidator;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;

@Rule(key = "S6304")
public class ResourceAccessPolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure granting access to all resources is safe here.";
  private static final String EFFECT_MESSAGE = "Related effect";
  private static final String ACTION_MESSAGE = "Related action";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    // Related to SONARIAC-260 and https://docs.aws.amazon.com/kms/latest/developerguide/key-policies.html
    // The use of 'Resource = "*"' is always safe, so we can ignore the resource for this rule.
    if (isResource(resource, "aws_kms_key")) {
      return;
    }
    PolicyUtils.getPolicies(resource).forEach(policy -> checkInsecurePolicy(ctx, policy));
  }

  private static void checkInsecurePolicy(CheckContext ctx, Policy policy) {
    PolicyValidator.findInsecureStatements(policy).forEach(statement -> ctx.reportIssue(statement.resource, MESSAGE, List.of(
      new SecondaryLocation(statement.effect, EFFECT_MESSAGE),
      new SecondaryLocation(statement.action, ACTION_MESSAGE))));
  }
}
