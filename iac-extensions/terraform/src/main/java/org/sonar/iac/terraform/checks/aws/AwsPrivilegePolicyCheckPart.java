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
package org.sonar.iac.terraform.checks.aws;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;

public class AwsPrivilegePolicyCheckPart extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure granting all privileges is safe here.";
  private static final String SECONDARY_MESSAGE = "Related effect";

  @Override
  protected void registerResourceChecks() {
    register((ctx, resource) -> PolicyUtils.getPolicies(resource).forEach(policy -> checkInsecurePolicy(ctx, policy)));
  }

  private static void checkInsecurePolicy(CheckContext ctx, Policy policy) {
    PolicyValidator.findInsecureStatements(policy)
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
      for (Policy.Statement statement : policy.statement()) {
        statement.action().flatMap(PolicyValidator::findInsecureAction)
          .ifPresent(action -> statement.effect().filter(PolicyValidator::isAllowEffect).ifPresent(effect -> result.add(new InsecureStatement(action, effect))));
        statement.notAction().flatMap(PolicyValidator::findInsecureAction)
          .ifPresent(notAction -> statement.effect().filter(PolicyValidator::isDenyEffect).ifPresent(effect -> result.add(new InsecureStatement(notAction, effect))));
      }
      return result;
    }

    private static Optional<Tree> findInsecureAction(Tree action) {
      if (action instanceof TupleTree actionTuple) {
        return actionTuple.elements().trees().stream().filter(PolicyValidator::applyToAnyAction).map(Tree.class::cast).findAny();
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
