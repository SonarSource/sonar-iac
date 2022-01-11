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

import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.checks.utils.PolicyUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Policy.Statement;

@Rule(key = "S6302")
public class PrivilegePolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure granting all privileges is safe here.";
  private static final String SECONDARY_MESSAGE = "Related effect";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    PolicyUtils.getPolicies(resource.properties()).stream()
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
      if (action instanceof SequenceTree) {
        return ((SequenceTree) action).elements().stream().filter(PolicyValidator::applyToAnyAction).map(Tree.class::cast).findAny();
      } else if (applyToAnyAction(action)) {
        return Optional.of(action);
      }
      return Optional.empty();
    }

    private static boolean applyToAnyAction(Tree action) {
      return TextUtils.isValue(action, "*").isTrue();
    }

    private static boolean isAllowEffect(Tree effect) {
      return TextUtils.isValue(effect, "Allow").isTrue();
    }

    private static boolean isDenyEffect(Tree effect) {
      return TextUtils.isValue(effect, "Deny").isTrue();
    }
  }
}
