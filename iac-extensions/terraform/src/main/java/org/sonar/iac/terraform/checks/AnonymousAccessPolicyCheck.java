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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.Policy.Statement;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.StatementTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;

@Rule(key = "S6270")
public class AnonymousAccessPolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure this policy granting anonymous access is safe here.";
  private static final String SECONDARY_MESSAGE = "Related effect.";

  @Override
  protected void registerChecks() {
    // do not register any check for a specific resource type
  }

  @Override
  public void initialize(InitContext init) {
    super.initialize(init);

    // in order to catch other statements, we need to register to other elements than resources
    init.register(BlockTree.class, (ctx, tree) -> {
      if (!isResource(tree)) {
        checkInsecureStatementsOutsideResources(ctx, tree);
      }
    });
  }

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    PolicyUtils.getPolicies(resource)
      .forEach(policy -> checkInsecurePolicy(ctx, policy));
  }

  private static void checkInsecurePolicy(CheckContext ctx, Policy policy) {
    PolicyValidator.findInsecureStatements(policy)
      .forEach(statement -> ctx.reportIssue(statement.principal, MESSAGE, new SecondaryLocation(statement.effect, SECONDARY_MESSAGE)));
  }

  private static void checkInsecureStatementsOutsideResources(CheckContext ctx, BlockTree tree) {
    PolicyValidator.findInsecureStatements(tree)
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

    public static Collection<InsecureStatement> findInsecureStatements(Policy policy) {
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

    public static Collection<InsecureStatement> findInsecureStatements(BlockTree nonResource) {
      List<InsecureStatement> results = new ArrayList<>();
      for (StatementTree statement : nonResource.properties()) {
        if ("statement".equalsIgnoreCase(statement.key().value())) {
          PropertyUtils.value(statement, "effect")
            .filter(PolicyValidator::isAllowEffect)
            .ifPresent(effect -> PropertyUtils.value(statement, "principals", BodyTree.class)
              .map(BodyTree::statements)
              .filter(PolicyValidator::hasAwsType)
              .flatMap(PolicyValidator::findInsecurePrincipal)
              .ifPresent(principal -> results.add(new InsecureStatement(principal, effect))));
          PropertyUtils.value(statement, "effect")
            .filter(PolicyValidator::isDenyEffect)
            .ifPresent(effect -> PropertyUtils.value(statement, "not_principals", BodyTree.class)
              .map(BodyTree::statements)
              .filter(PolicyValidator::hasAwsType)
              .flatMap(PolicyValidator::findInsecurePrincipal)
              .ifPresent(notPrincipals -> results.add(new InsecureStatement(notPrincipals, effect))));
        }
      }
      return results;
    }

    private static Optional<Tree> findInsecurePrincipal(Tree principal) {
      if (principal instanceof ObjectTree) {
        return findInsecurePrincipal((ObjectTree) principal);
      }
      if (principal instanceof TupleTree) {
        return findInsecurePrincipal((TupleTree) principal);
      }
      if (applyToAnyPrincipal(principal)) {
        return Optional.of(principal);
      }
      return Optional.empty();
    }

    private static Optional<Tree> findInsecurePrincipal(TupleTree tupleTree) {
      return tupleTree.elements()
        .trees()
        .stream()
        .filter(PolicyValidator::applyToAnyPrincipal)
        .map(Tree.class::cast)
        .findAny();
    }

    private static Optional<Tree> findInsecurePrincipal(ObjectTree principal) {
      return PropertyUtils.get(principal, "AWS")
        .map(PropertyTree::value)
        .flatMap(PolicyValidator::findInsecurePrincipal);
    }

    private static Optional<Tree> findInsecurePrincipal(List<StatementTree> statements) {
      return statements.stream()
        .map(PolicyValidator::findInsercurePrincipal)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private static Optional<Tree> findInsercurePrincipal(StatementTree principal) {
      if ("identifiers".equalsIgnoreCase(principal.key().value())) {
        return findInsecurePrincipal(principal.value());
      }
      return Optional.empty();
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

    private static boolean hasAwsType(List<StatementTree> statements) {
      return statements.stream()
        .filter(statement -> "type".equalsIgnoreCase(statement.key().value()))
        .map(StatementTree::value)
        .anyMatch(value -> hasTextValue(value, "AWS"));
    }
  }
}
