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
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.checks.utils.PolicyUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Policy.Statement;

@Rule(key = "S6304")
public class ResourceAccessPolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure granting access to all resources is safe here.";
  private static final String SECONDARY_MESSAGE = "Related effect";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    PolicyUtils.getPolicies(resource.properties()).stream()
      .forEach(policy -> checkInsecurePolicy(ctx, policy));
  }

  private static void checkInsecurePolicy(CheckContext ctx, Policy policy) {
    PolicyValidator.findInsecureStatements(policy)
      .forEach(statement -> ctx.reportIssue(statement.resource, MESSAGE, new SecondaryLocation(statement.effect, SECONDARY_MESSAGE)));
  }

  private static class InsecureStatement {
    final Tree resource;
    final Tree effect;

    public InsecureStatement(Tree action, Tree effect) {
      this.resource = action;
      this.effect = effect;
    }
  }

  private static class PolicyValidator {

    static List<InsecureStatement> findInsecureStatements(Policy policy) {
      List<InsecureStatement> result = new ArrayList<>();
      for (Statement statement : policy.statement()) {
        statement.resource().flatMap(PolicyValidator::findInsecureResource).ifPresent(resource ->
          statement.effect().filter(PolicyValidator::isAllowEffect).ifPresent(effect -> 
            result.add(new InsecureStatement(resource, effect))
        ));
        statement.notResource().flatMap(PolicyValidator::findInsecureResource).ifPresent(notResource ->
          statement.effect().filter(PolicyValidator::isDenyEffect).ifPresent(effect ->
            result.add(new InsecureStatement(notResource, effect))
        ));
      }
      return result;
    }

    private static Optional<Tree> findInsecureResource(Tree resource) {
      if (resource instanceof SequenceTree) {
        return ((SequenceTree) resource).elements().stream().filter(PolicyValidator::applyToAnyResource).map(Tree.class::cast).findAny();
      } else if (applyToAnyResource(resource)) {
        return Optional.of(resource);
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
