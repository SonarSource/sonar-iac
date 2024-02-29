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
import org.sonar.iac.common.yaml.block.BlockBlock;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6868")
public class CommandExecutionCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Remove the command execution permission for this role.";
  private static final List<String> SENSITIVE_KINDS = List.of("Role", "ClusterRole");

  @Override
  boolean shouldVisitWholeDocument() {
    return true;
  }

  @Override
  void registerObjectCheck() {
    register(SENSITIVE_KINDS, document -> document.blocks("rules")
      .filter(CommandExecutionCheck::ruleContainsSensitiveVerb)
      .filter(CommandExecutionCheck::ruleContainsSensitiveResource)
      .forEach(rule -> rule.attribute("resources").reportOnKey(MESSAGE)));
  }

  private static boolean ruleContainsSensitiveVerb(BlockBlock rule) {
    return containsSensitiveItemOrWildCard(rule, "verbs", "create");
  }

  private static boolean ruleContainsSensitiveResource(BlockBlock rule) {
    return containsSensitiveItemOrWildCard(rule, "resources", "pods/exec");
  }

  private static boolean containsSensitiveItemOrWildCard(BlockBlock rule, String listKey, String sensitiveItem) {
    Predicate<YamlTree> verbsPredicate = TreePredicates.isEqualTo(sensitiveItem).or(TreePredicates.isEqualTo("*"));
    return rule.list(listKey).getItemIf(verbsPredicate).findAny().isPresent();
  }
}
