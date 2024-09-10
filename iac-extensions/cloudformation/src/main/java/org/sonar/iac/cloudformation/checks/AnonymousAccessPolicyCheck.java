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
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.checks.utils.PolicyUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.checks.policy.Policy.Statement;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;

@Rule(key = "S6270")
public class AnonymousAccessPolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure granting public access is safe here.";
  private static final String SECONDARY_MESSAGE = "Related effect.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    PolicyUtils.getPolicies(resource.properties())
      .forEach(policy -> checkInsecurePolicy(ctx, policy));
  }

  private static void checkInsecurePolicy(CheckContext ctx, Policy policy) {
    Optional.of(policy)
      .filter(AnonymousAccessPolicyCheck::hasNoConditions)
      .map(PolicyValidator::findInsecureStatements)
      .orElse(List.of())
      .forEach(statement -> ctx.reportIssue(statement.principal, MESSAGE, new SecondaryLocation(statement.effect, SECONDARY_MESSAGE)));
  }

  private static boolean hasNoConditions(Policy policy) {
    return policy.statement().stream().noneMatch(statement -> statement.condition().isPresent());
  }

  private record InsecureStatement(Tree principal, Tree effect) {
  }

  private static class PolicyValidator {

    static List<InsecureStatement> findInsecureStatements(Policy policy) {
      List<InsecureStatement> result = new ArrayList<>();
      for (Statement statement : policy.statement()) {
        statement.effect()
          .filter(PolicyValidator::isAllowEffect)
          .ifPresent(effect -> statement.principal()
            .flatMap(PolicyValidator::findInsecurePrincipal)
            .ifPresent(principal -> result.add(new InsecureStatement(principal, effect))));
        statement.effect()
          .filter(PolicyValidator::isDenyEffect)
          .ifPresent(effect -> statement.notPrincipal()
            .flatMap(PolicyValidator::findInsecurePrincipal)
            .ifPresent(notPrincipal -> result.add(new InsecureStatement(notPrincipal, effect))));
      }
      return result;
    }

    private static Optional<Tree> findInsecurePrincipal(Tree principal) {
      if (principal instanceof MappingTree mappingTree) {
        return findInsecurePrincipal(mappingTree);
      }
      if (principal instanceof SequenceTree sequenceTree) {
        return findInsecurePrincipal(sequenceTree);
      }
      if (applyToAnyPrincipal(principal)) {
        return Optional.of(principal);
      }
      return Optional.empty();
    }

    private static Optional<Tree> findInsecurePrincipal(MappingTree principal) {
      return principal.properties()
        .stream()
        .filter(prop -> isAwsPrincipal(prop.key()))
        .map(PropertyTree::value)
        .map(PolicyValidator::findInsecurePrincipal)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private static Optional<Tree> findInsecurePrincipal(SequenceTree principal) {
      return principal.elements()
        .stream()
        .filter(PolicyValidator::applyToAnyPrincipal)
        .map(Tree.class::cast)
        .findFirst();
    }

    private static boolean isAwsPrincipal(Tree principal) {
      return hasTextValue(principal, "AWS");
    }

    private static boolean applyToAnyPrincipal(Tree action) {
      return hasTextValue(action, "*");
    }

    private static boolean isAllowEffect(Tree effect) {
      return hasTextValue(effect, "Allow");
    }

    private static boolean isDenyEffect(Tree effect) {
      return hasTextValue(effect, "Deny");
    }

    private static boolean hasTextValue(Tree tree, String value) {
      return TextUtils.isValue(tree, value).isTrue();
    }
  }
}
