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
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.checks.utils.PolicyUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PrivilegeEscalationVector;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.checks.policy.Policy.Statement;
import org.sonar.iac.common.yaml.tree.SequenceTree;

@Rule(key = "S6317")
public class PrivilegeEscalationCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "This policy is vulnerable to the %s privilege escalation vector. Remove " +
    "permissions or restrict the set of resources they apply to";
  private static final Pattern RESOURCE_NAME_PATTERN = Pattern.compile("arn:[^:]*:[^:]*:[^:]*:[^:]*:(role|user|group)/\\*");

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!resource.isType("AWS::IAM::ManagedPolicy")) {
      return;
    }
    PolicyUtils.getPolicies(resource.properties())
      .forEach(policy -> checkPrivilegeEscalation(ctx, policy));
  }

  private static void checkPrivilegeEscalation(CheckContext ctx, Policy policy) {
    /*for (Policy.Statement policyStatement : policy.statement()) {
      Optional<PrivilegeEscalationVector> privilegeEscalationVectorOptional = allowsPrivilegeEscalation(policyStatement);
      if (privilegeEscalationVectorOptional.isPresent()) {
        PrivilegeEscalationVector privilegeEscalationVector = privilegeEscalationVectorOptional.get();
        String vectorName = privilegeEscalationVector.getVectorName();

        List<SecondaryLocation> secondaryLocations = new ArrayList<>();
        for (Tree actionElement : ((SequenceTree) policyStatement.action().get()).elements()) {
          Optional<String> value = TextUtils.getValue(actionElement);
          if (value.isPresent() && (privilegeEscalationVector.getStringPermissions().contains(value.get()))) {
            if (privilegeEscalationVector.getStringPermissions().size() == 1) {
              secondaryLocations.add(new SecondaryLocation(actionElement, "Single Permission"));
            } else if (privilegeEscalationVector.getStringPermissions().size() > 1) {
              secondaryLocations.add(new SecondaryLocation(actionElement, "Multiple Permissions"));
            }

          }
        }

        ctx.reportIssue(policyStatement.resource().get(), String.format(MESSAGE, vectorName), secondaryLocations);
      }
      */
    //collect statements that
    List<Statement> statementsWithPrivilegeEscalation = policy.statement().stream()
      .filter(statement -> allowsPrivilegeEscalation(statement).isPresent())
      .collect(Collectors.toList());

    statementsWithPrivilegeEscalation.forEach(statement -> {
      PrivilegeEscalationVector vector = allowsPrivilegeEscalation(statement).get();
      String vectorName = vector.getVectorName();
      List<SecondaryLocation> secondaryLocations = new ArrayList<>();
      ((SequenceTree) statement.action().get()).elements().stream()
        .filter(actionElement -> TextUtils.getValue(actionElement).isPresent() && vector.getStringPermissions().contains(TextUtils.getValue(actionElement).get()))
        .forEach(actionElement -> {
          if (vector.getStringPermissions().size() == 1) {
            secondaryLocations.add(new SecondaryLocation(actionElement, "Single Permission"));
          } else if (vector.getStringPermissions().size() > 1) {
            secondaryLocations.add(new SecondaryLocation(actionElement, "Multiple Permissions"));
          }
        });
      ctx.reportIssue(statement.resource().get(), String.format(MESSAGE, vectorName), secondaryLocations);
    });
  }


  private static Optional<PrivilegeEscalationVector> allowsPrivilegeEscalation(Statement statement) {
    //ToDo: change this
    boolean testBoolean = statement.effect().filter(PrivilegeEscalationCheck::isAllowEffect).isPresent()
      && statement.resource().filter(PrivilegeEscalationCheck::isSensitiveResource).isPresent()
      && statement.condition().isEmpty()
      && statement.principal().isEmpty();

    if (testBoolean) {
      return isSensitiveAction(statement.action().get());
    }
//    statement.action().filter(PrivilegeEscalationCheck::isSensitiveAction).isPresent()
    return Optional.empty();
  }

  private static boolean isAllowEffect(Tree effect) {
    return TextUtils.isValue(effect, "Allow").isTrue();
  }

  private static boolean isSensitiveResource(Tree resource) {
    return TextUtils.matchesValue(resource, rsc -> rsc.equals("*") || RESOURCE_NAME_PATTERN.matcher(rsc).matches()).isTrue();
  }

  private static Optional<PrivilegeEscalationVector> isSensitiveAction(Tree action) {
    if (!(action instanceof SequenceTree)) {
      return Optional.empty();
    }

    Stream<String> actionPermissions = ((SequenceTree) action).elements().stream().map(TextUtils::getValue).flatMap(Optional::stream);
    return PrivilegeEscalationVector.getEscalationVector(actionPermissions);
  }
}
