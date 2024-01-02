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
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PrivilegeEscalationVector;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.checks.policy.Policy.Statement;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

@Rule(key = "S6317")
public class PrivilegeEscalationCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "This policy is vulnerable to the \"%s\" privilege escalation vector. Remove permissions or " +
    "restrict the set of resources they apply to.";
  private static final String MESSAGE_ACTION_MULTIPLE = "When combined with others, this permission enables the \"%s\" escalation vector.";
  private static final String MESSAGE_ACTION_SINGLE = "This permission enables the \"%s\" escalation vector.";
  private static final String MESSAGE_STATEMENT_ALL = "Permissions are granted on all resources.";

  @Override
  protected void registerResourceConsumer() {
    register("aws_iam_policy",
      resource -> PolicyUtils.getPolicies(resource.tree)
        .forEach(policy -> checkPrivilegeEscalation(resource, policy)));
  }

  private static void checkPrivilegeEscalation(ResourceSymbol resourceSymbol, Policy policy) {
    for (Statement statement : policy.statement()) {
      Optional<Tree> action = statement.action();
      if (action.isPresent() && action.get() instanceof TupleTree) {
        List<Tree> actionTrees = ((TupleTree) action.get()).elements().trees().stream().map(Tree.class::cast).collect(Collectors.toList());
        Optional<PrivilegeEscalationVector> vectorOpt = PrivilegeEscalationVector.getStatementEscalationVector(statement, actionTrees);
        if (vectorOpt.isPresent()) {
          PrivilegeEscalationVector vector = vectorOpt.get();
          List<SecondaryLocation> secondaryLocations = secondaryLocations(statement, vector);
          resourceSymbol.report(String.format(MESSAGE, vector.getName()), secondaryLocations);
        }
      }
    }
  }

  private static List<SecondaryLocation> secondaryLocations(Statement statement, PrivilegeEscalationVector vector) {
    List<SecondaryLocation> secondaryLocations = new ArrayList<>();
    secondaryLocations.addAll(retrieveSecondaryLocationsFromAction(statement, vector));
    secondaryLocations.addAll(retrieveSecondaryLocationsFromResource(statement));
    return secondaryLocations;
  }

  private static List<SecondaryLocation> retrieveSecondaryLocationsFromResource(Statement statement) {
    List<SecondaryLocation> secondaryLocationsFromResource = new ArrayList<>();
    statement.resource().ifPresent(resource -> {
      if (TextUtils.isValue(resource, "*").isTrue()) {
        secondaryLocationsFromResource.add(new SecondaryLocation(resource, MESSAGE_STATEMENT_ALL));
      }
    });
    return secondaryLocationsFromResource;
  }

  private static List<SecondaryLocation> retrieveSecondaryLocationsFromAction(Statement statement, PrivilegeEscalationVector vector) {
    List<SecondaryLocation> secondaryLocationsFromAction = new ArrayList<>();
    String actionsMsg = vector.getPermissions().size() == 1 ? String.format(MESSAGE_ACTION_SINGLE, vector.getName()) : String.format(MESSAGE_ACTION_MULTIPLE, vector.getName());
    statement.action().ifPresent(tree -> ((TupleTree) tree).elements().trees().stream()
      .filter(actionElement -> TextUtils.getValue(actionElement)
        .map(value -> PrivilegeEscalationVector.actionEnablesVector(vector, value))
        .orElse(false))
      .forEach(actionElement -> secondaryLocationsFromAction.add(new SecondaryLocation(actionElement, actionsMsg))));
    return secondaryLocationsFromAction;
  }
}
