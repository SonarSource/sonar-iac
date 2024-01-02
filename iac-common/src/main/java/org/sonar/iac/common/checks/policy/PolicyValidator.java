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
package org.sonar.iac.common.checks.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.ResourceAccessPolicyVector;
import org.sonar.iac.common.checks.TextUtils;

public class PolicyValidator {

  private PolicyValidator() {
  }

  public static List<PolicyValidator.InsecureStatement> findInsecureStatements(Policy policy) {
    List<PolicyValidator.InsecureStatement> result = new ArrayList<>();
    for (Policy.Statement statement : policy.statement()) {
      Tree resourceAccessAction = statement.action().flatMap(PolicyValidator::findResourceAccessAction).orElse(null);
      if (resourceAccessAction == null) {
        continue;
      }

      statement.resource().flatMap(PolicyValidator::findInsecureResource).ifPresent(resource -> statement.effect().filter(PolicyValidator::isAllowEffect)
        .ifPresent(effect -> result.add(new PolicyValidator.InsecureStatement(resource, effect, resourceAccessAction))));

      statement.notResource().flatMap(PolicyValidator::findInsecureResource).ifPresent(notResource -> statement.effect().filter(PolicyValidator::isDenyEffect)
        .ifPresent(effect -> result.add(new PolicyValidator.InsecureStatement(notResource, effect, resourceAccessAction))));
    }
    return result;
  }

  /**
   * Statement values can be a single string or a list of elements. This method examines the value for a given predicate
   * and returns an optional object if the single element or at least one element matches the predicate.
   */
  static Optional<Tree> explore(Predicate<Tree> predicate, Tree tree) {
    if (tree instanceof Iterable) {
      return StreamSupport.stream(((Iterable<?>) tree).spliterator(), false)
        .filter(Tree.class::isInstance)
        .map(Tree.class::cast)
        .filter(predicate)
        .findAny();
    } else if (predicate.test(tree)) {
      return Optional.of(tree);
    }
    return Optional.empty();
  }

  static Optional<Tree> findResourceAccessAction(Tree action) {
    return explore(ResourceAccessPolicyVector::isResourceAccessPolicy, action);
  }

  static Optional<Tree> findInsecureResource(Tree resource) {
    return explore(PolicyValidator::applyToAnyResource, resource);
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

  public static class InsecureStatement {
    public final Tree resource;
    public final Tree effect;
    public final Tree action;

    public InsecureStatement(Tree resource, Tree effect, Tree action) {
      this.resource = resource;
      this.effect = effect;
      this.action = action;
    }
  }
}
