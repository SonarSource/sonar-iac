/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.checks.utils.PolicyUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Policy.Statement;

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
  protected void checkResource(CheckContext ctx, Resource resource) {
    PolicyUtils.getPolicies(resource.properties())
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
    return action instanceof SequenceTree && ((SequenceTree) action).elements().stream()
        .map(TextUtils::getValue)
        .anyMatch(act -> act.filter(a -> PREDEFINED_WILDCARD_ACTIONS.contains(a) || PREDEFINED_ACTIONS.contains(a)).isPresent());
  }
}
