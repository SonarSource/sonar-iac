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
package org.sonar.iac.kubernetes.checks;

import java.util.List;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.common.yaml.TreePredicates;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6867")
public class RBACWildcardCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Do not use wildcards when defining RBAC permissions.";
  private static final List<String> SENSITIVE_KINDS = List.of("Role", "ClusterRole");

  @Override
  boolean shouldVisitWholeDocument() {
    return true;
  }

  @Override
  void registerObjectCheck() {
    register(SENSITIVE_KINDS, document -> document.blocks("rules")
      .forEach((BlockObject rule) -> {
        if (containsWildCardItem(rule, "resources")) {
          reportOnKey(rule, "resources");
        }
        if (containsWildCardItem(rule, "verbs")) {
          reportOnKey(rule, "verbs");
        }
      }));
  }

  private static boolean containsWildCardItem(BlockObject rule, String listKey) {
    Predicate<YamlTree> wildcardPredicate = TreePredicates.isEqualTo("*");
    return rule.list(listKey).getItemIf(wildcardPredicate).findAny().isPresent();
  }

  void reportOnKey(BlockObject rule, String key) {
    TupleTree resourcesTree = rule.attribute(key).tree;
    if (resourcesTree != null) {
      rule.ctx.reportIssue(resourcesTree.key().metadata().textRange(), MESSAGE);
    }
  }
}
