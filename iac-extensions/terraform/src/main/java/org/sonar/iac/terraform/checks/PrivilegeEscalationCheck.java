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

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.checks.policy.Policy.Statement;
import org.sonar.iac.common.checks.PrivilegeEscalationVector;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;

@Rule(key = "S6317")
public class PrivilegeEscalationCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Narrow these permissions to a smaller set of resources to avoid privilege escalation.";
  private static final Pattern RESOURCE_NAME_PATTERN = Pattern.compile("arn:[^:]*:[^:]*:[^:]*:[^:]*:(role|user|group)/\\*");

  @Override
  protected void registerResourceConsumer() {
    register("aws_iam_policy",
      resource -> PolicyUtils.getPolicies(resource.tree)
        .forEach(policy -> checkPrivilegeEscalation(resource.ctx, policy)));
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
      && statement.condition().isEmpty()
      && statement.principal().isEmpty();
  }

  private static boolean isAllowEffect(Tree effect) {
    return TextUtils.isValue(effect, "Allow").isTrue();
  }

  private static boolean isSensitiveResource(Tree resource) {
    return TextUtils.matchesValue(resource, rsc -> rsc.equals("*") || RESOURCE_NAME_PATTERN.matcher(rsc).matches()).isTrue();
  }

  private static boolean isSensitiveAction(Tree action) {
    if (!(action instanceof TupleTree)) {
      return false;
    }

    Stream<String> actionPermissions = ((TupleTree) action).elements().trees().stream().map(TextUtils::getValue).flatMap(Optional::stream);
    return PrivilegeEscalationVector.isSupersetOfAnEscalationVector(actionPermissions);
  }
}
