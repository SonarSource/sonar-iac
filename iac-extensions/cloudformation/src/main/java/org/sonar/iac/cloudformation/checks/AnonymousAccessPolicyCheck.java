/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.checks.utils.PolicyUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.Policy.Statement;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6270")
public class AnonymousAccessPolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure this policy granting anonymous access is safe here.";
  private static final String SECONDARY_MESSAGE = "Related effect.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    PolicyUtils.getPolicies(resource.properties())
      .forEach(policy -> checkInsecurePolicy(ctx, policy));
  }

  private static void checkInsecurePolicy(CheckContext ctx, Policy policy) {
    PolicyValidator.findInsecureStatements(policy)
      .forEach(statement -> ctx.reportIssue(statement.principal, MESSAGE, new SecondaryLocation(statement.effect, SECONDARY_MESSAGE)));
  }

  private static class InsecureStatement {
    final Tree principal;
    final Tree effect;

    public InsecureStatement(Tree principal, Tree effect) {
      this.principal = principal;
      this.effect = effect;
    }
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
      if (principal instanceof MappingTree) {
        return findInsecurePrincipal((MappingTree) principal);
      }
      if (principal instanceof SequenceTree) {
        return findInsecurePrincipal((SequenceTree) principal);
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
