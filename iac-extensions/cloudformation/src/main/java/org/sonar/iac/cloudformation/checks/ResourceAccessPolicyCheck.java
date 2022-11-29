/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.checks.utils.PolicyUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.Policy.Statement;
import org.sonar.iac.common.checks.ResourceAccessPolicyVector;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.SequenceTree;

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
    PolicyValidator.findInsecureStatements(policy).forEach(statement ->
      ctx.reportIssue(statement.resource, MESSAGE, List.of(
        new SecondaryLocation(statement.effect, EFFECT_MESSAGE),
        new SecondaryLocation(statement.action, ACTION_MESSAGE)
      )));
  }

  private static class InsecureStatement {
    final Tree resource;
    final Tree effect;
    final Tree action;

    public InsecureStatement(Tree resource, Tree effect, Tree action) {
      this.resource = resource;
      this.effect = effect;
      this.action = action;
    }
  }

  private static class PolicyValidator {

    static List<InsecureStatement> findInsecureStatements(Policy policy) {
      List<InsecureStatement> result = new ArrayList<>();
      for (Statement statement : policy.statement()) {
        Tree resourceAccessAction = statement.action().flatMap(PolicyValidator::findResourceAccessAction).orElse(null);
        if (resourceAccessAction == null) {
          continue;
        }

        statement.resource().flatMap(PolicyValidator::findInsecureResource).ifPresent(resource ->
          statement.effect().filter(PolicyValidator::isAllowEffect).ifPresent(effect -> 
            result.add(new InsecureStatement(resource, effect, resourceAccessAction))
        ));
        statement.notResource().flatMap(PolicyValidator::findInsecureResource).ifPresent(notResource ->
          statement.effect().filter(PolicyValidator::isDenyEffect).ifPresent(effect ->
            result.add(new InsecureStatement(notResource, effect, resourceAccessAction))
        ));
      }
      return result;
    }

    private static Optional<Tree> findResourceAccessAction(Tree action) {
      return explore(ResourceAccessPolicyVector::isResourceAccessPolicy, action);
    }

    private static Optional<Tree> findInsecureResource(Tree resource) {
      return explore(PolicyValidator::applyToAnyResource, resource);
    }

    private static Optional<Tree> explore(Predicate<Tree> predicate, Tree tree) {
      if (tree instanceof SequenceTree) {
        return ((SequenceTree) tree).elements().stream().filter(predicate).map(Tree.class::cast).findAny();
      } else if (predicate.test(tree)) {
        return Optional.of(tree);
      }
      return Optional.empty();
    }

    private static boolean applyToAnyResource(Tree resource) {
      return TextUtils.isValue(resource, "*").isTrue();
    }

    private static boolean isAllowEffect(Tree effect) {
      return TextUtils.isValue(effect, "Allow").isTrue();
    }

    private static boolean isDenyEffect(Tree effect) {
      return TextUtils.isValue(effect, "Deny").isTrue();
    }
  }
}
