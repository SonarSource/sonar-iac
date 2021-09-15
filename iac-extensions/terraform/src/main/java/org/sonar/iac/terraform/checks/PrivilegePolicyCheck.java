/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Policy.Statement;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;

@Rule(key = "S6302")
public class PrivilegePolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure granting all privileges is safe here.";
  private static final String SECONDARY_MESSAGE = "Related effect";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    PolicyUtils.getPolicies(resource).stream()
      .forEach(policy -> checkInsecurePolicy(ctx, policy));
  }

  private static void checkInsecurePolicy(CheckContext ctx, Policy policy) {
    PolicyValidator.findInsecureStatements(policy).stream()
      .forEach(statement -> ctx.reportIssue(statement.action, MESSAGE, new SecondaryLocation(statement.effect, SECONDARY_MESSAGE)));
  }

  private static class InsecureStatement {
    final Tree action;
    final Tree effect;

    public InsecureStatement(Tree action, Tree effect) {
      this.action = action;
      this.effect = effect;
    }
  }

  private static class PolicyValidator {

    static List<InsecureStatement> findInsecureStatements(Policy policy) {
      List<InsecureStatement> result = new ArrayList<>();
      for (Statement statement : policy.statement()) {
        statement.action().flatMap(PolicyValidator::findInsecureAction).ifPresent(action ->
          statement.effect().filter(PolicyValidator::isAllowEffect).ifPresent(effect -> 
            result.add(new InsecureStatement(action, effect))
        ));
        statement.notAction().flatMap(PolicyValidator::findInsecureAction).ifPresent(notAction ->
          statement.effect().filter(PolicyValidator::isDenyEffect).ifPresent(effect ->
            result.add(new InsecureStatement(notAction, effect))
        ));
      }
      return result;
    }

    private static Optional<Tree> findInsecureAction(Tree action) {
      if (action instanceof TupleTree) {
        return ((TupleTree) action).elements().trees().stream().filter(PolicyValidator::applyToAnyAction).map(Tree.class::cast).findAny();
      } else if (applyToAnyAction(action)) {
        return Optional.of(action);
      }
      return Optional.empty();
    }

    private static boolean applyToAnyAction(Tree rule) {
      return TextUtils.isValue(rule, "*").isTrue();
    }

    private static boolean isAllowEffect(Tree effect) {
      return TextUtils.isValue(effect, "Allow").isTrue();
    }

    private static boolean isDenyEffect(Tree effect) {
      return TextUtils.isValue(effect, "Deny").isTrue();
    }
  }
}
