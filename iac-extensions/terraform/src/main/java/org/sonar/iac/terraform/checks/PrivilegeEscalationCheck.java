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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.checks.policy.Policy.Statement;
import org.sonar.iac.common.checks.PrivilegeEscalationVector;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

@Rule(key = "S6317")
public class PrivilegeEscalationCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "This policy is vulnerable to the \"%s\" privilege escalation vector. Remove permissions or restrict the set of resources they apply to.";
  private static final String MESSAGE_ACTION_MULTIPLE = "When combined with others, this permission enables the \"%s\" escalation vector.";
  private static final String MESSAGE_ACTION_SINGLE = "This permission enables the \"%s\" escalation vector.";
  private static final String MESSAGE_STATEMENT_ALL = "Permissions are granted on all resources.";
  private static final Pattern RESOURCE_NAME_PATTERN = Pattern.compile("arn:[^:]*:[^:]*:[^:]*:[^:]*:(role|user|group)/\\*");

  @Override
  protected void registerResourceConsumer() {
    register("aws_iam_policy",
      resource ->  PolicyUtils.getPolicies(resource.tree)
        .forEach(policy -> checkPrivilegeEscalation(resource, policy)));
  }

  private static void checkPrivilegeEscalation(ResourceSymbol resourceSymbol, Policy policy) {
    for (Statement statement : policy.statement()) {
      Optional<PrivilegeEscalationVector> vectorOpt = getStatementEscalationVector(statement);
      if (vectorOpt.isPresent()) {
        PrivilegeEscalationVector vector = vectorOpt.get();
        String vectorName = vector.getName();
        List<SecondaryLocation> secondaryLocations = secondaryLocations(statement, vector, vectorName);
        resourceSymbol.report(String.format(MESSAGE, vectorName), secondaryLocations);
      }
    }
  }

  private static List<SecondaryLocation> secondaryLocations(Statement statement, PrivilegeEscalationVector vector, String vectorName) {
    List<SecondaryLocation> secondaryLocations = new ArrayList<>();
    String actionsMsg = vector.getPermissions().size() == 1 ? String.format(MESSAGE_ACTION_SINGLE, vectorName) :
      String.format(MESSAGE_ACTION_MULTIPLE, vectorName);
    statement.action().ifPresent(tree -> ((TupleTree) tree).elements().trees().stream()
      .filter(actionElement -> TextUtils.getValue(actionElement).map(value -> actionEnablesVector(vector, value)).orElse(false))
      .forEach(actionElement -> secondaryLocations.add(new SecondaryLocation(actionElement, String.format(actionsMsg, vectorName)))));
    statement.resource().ifPresent(resource -> {
      if (TextUtils.isValue(resource, "*").isTrue()) {
        secondaryLocations.add(new SecondaryLocation(resource, MESSAGE_STATEMENT_ALL));
      }
    });
    return secondaryLocations;
  }

  private static boolean actionEnablesVector(PrivilegeEscalationVector vector, String value) {
    PrivilegeEscalationVector.Permission permission = PrivilegeEscalationVector.Permission.of(value);
    return vector.getPermissions().stream().anyMatch(p -> p.isCoveredBy(permission));
  }

  private static Optional<PrivilegeEscalationVector> getStatementEscalationVector(Statement statement) {
    Optional<Tree> action = statement.action();
    if (statement.effect().filter(PrivilegeEscalationCheck::isAllowEffect).isPresent()
      && statement.resource().filter(PrivilegeEscalationCheck::isSensitiveResource).isPresent()
      && statement.condition().isEmpty()
      && statement.principal().isEmpty()
      && action.isPresent()) {
      return getActionEscalationVector(action.get());
    }
    return Optional.empty();
  }


  private static boolean isAllowEffect(Tree effect) {
    return TextUtils.isValue(effect, "Allow").isTrue();
  }

  private static boolean isSensitiveResource(Tree resource) {
    return TextUtils.matchesValue(resource, rsc -> rsc.equals("*") || RESOURCE_NAME_PATTERN.matcher(rsc).matches()).isTrue();
  }

  private static Optional<PrivilegeEscalationVector> getActionEscalationVector(Tree action) {
    if (!(action instanceof TupleTree)) {
      return Optional.empty();
    }

    Stream<String> actionPermissions = ((TupleTree) action).elements().trees().stream().map(TextUtils::getValue).flatMap(Optional::stream);
    return PrivilegeEscalationVector.getEscalationVector(actionPermissions);
  }
}
