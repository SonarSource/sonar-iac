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
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.TreePredicates;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S6867")
public class RBACWildcardCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Do not use wildcards when defining RBAC permissions.";
  private static final List<String> SENSITIVE_KINDS = List.of("Role", "ClusterRole");
  private static final List<String> SENSITIVE_RULE_ATTRIBUTES = List.of("resources", "verbs");

  @Override
  boolean shouldVisitWholeDocument() {
    return true;
  }

  @Override
  void registerObjectCheck() {
    register(SENSITIVE_KINDS, document -> document.blocks("rules")
      .forEach((BlockObject rule) -> SENSITIVE_RULE_ATTRIBUTES.forEach((String attributeKey) -> {
        if (containsWildCardItem(rule, attributeKey)) {
          rule.attribute(attributeKey).reportOnValue(MESSAGE);
        }
      })));
  }

  @Override
  void initializeCheck(CheckContext ctx) {
    if (ctx instanceof KubernetesCheckContext helmContext) {
      helmContext.setShouldReportSecondaryInValues(true);
    }
  }

  private static boolean containsWildCardItem(BlockObject rule, String listKey) {
    Predicate<YamlTree> wildcardPredicate = TreePredicates.isEqualTo("*");
    return rule.list(listKey).getItemIf(wildcardPredicate).findAny().isPresent();
  }
}
