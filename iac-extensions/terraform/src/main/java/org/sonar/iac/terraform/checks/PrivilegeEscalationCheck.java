/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Policy.Statement;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;

@Rule(key = "S6317")
public class PrivilegeEscalationCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Narrow these permissions to a smaller set of resources to avoid privilege escalation.";
  private static final Pattern RESOURCE_NAME_PATTERN = Pattern.compile("arn:[^:]*:[^:]*:[^:]*:[^:]*:(role|user|group)/\\*");
  private static final Set<String> PREDEFINED_WILDCARD_ACTIONS = new HashSet<>(Arrays.asList(
    "iam:*",
    "sts:*",
    "ec2:*",
    "lambda:*",
    "cloudformation:*",
    "datapipeline:*",
    "glue:*"
  ));
  private static final Set<String> PREDEFINED_ACTIONS = new HashSet<>(Arrays.asList(
    "iam:CreatePolicyVersion",
    "iam:SetDefaultPolicyVersion",
    "iam:CreateAccessKey",
    "iam:CreateLoginProfile",
    "iam:UpdateLoginProfile",
    "iam:AttachUserPolicy",
    "iam:AttachGroupPolicy",
    "iam:AttachRolePolicy",
    "sts:AssumeRole",
    "iam:PutUserPolicy",
    "iam:PutGroupPolicy",
    "iam:PutRolePolicy",
    "iam:AddUserToGroup",
    "iam:UpdateAssumeRolePolicy",
    "iam:PassRole",
    "ec2:RunInstances",
    "lambda:CreateFunction",
    "lambda:InvokeFunction",
    "lambda:AddPermission",
    "lambda:CreateEventSourceMapping",
    "cloudformation:CreateStack",
    "datapipeline:CreatePipeline",
    "datapipeline:PutPipelineDefinition",
    "glue:CreateDevEndpoint",
    "glue:UpdateDevEndpoint",
    "lambda:UpdateFunctionCode"
  ));

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    PolicyUtils.getPolicies(resource)
      .forEach(policy -> checkPrivilegeEscalation(ctx, policy));
  }

  private static void checkPrivilegeEscalation(CheckContext ctx, Policy policy) {
    policy.statement().stream()
      .filter(PrivilegeEscalationCheck::allowsPrivilegeEscalation)
      .forEach(statement -> ctx.reportIssue(statement.resource().get(), MESSAGE));
  }

  private static boolean allowsPrivilegeEscalation(Statement statement) {
    return statement.effect().filter(PrivilegeEscalationCheck::isAllowEffect).isPresent()
        && statement.resource().filter(PrivilegeEscalationCheck::isSensitiveResource).isPresent()
        && statement.action().filter(PrivilegeEscalationCheck::isSensitiveAction).isPresent()
        && !statement.condition().isPresent()
        && !statement.principal().isPresent();
  }

  private static boolean isAllowEffect(Tree effect) {
    return TextUtils.isValue(effect, "Allow").isTrue();
  }

  private static boolean isSensitiveResource(Tree resource) {
    return TextUtils.matchesValue(resource, rsc -> rsc.equals("*") || RESOURCE_NAME_PATTERN.matcher(rsc).matches()).isTrue();
  }

  private static boolean isSensitiveAction(Tree action) {
    return action instanceof TupleTree && ((TupleTree) action).elements().trees().stream()
        .map(TextUtils::getValue)
        .anyMatch(act -> act.filter(a -> PREDEFINED_WILDCARD_ACTIONS.contains(a) || PREDEFINED_ACTIONS.contains(a)).isPresent());
  }
}
