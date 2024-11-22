/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.checks;

import java.util.List;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.common.yaml.TreePredicates;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6868")
public class CommandExecutionCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Remove the command execution permission for this role.";
  private static final List<String> SENSITIVE_KINDS = List.of("Role", "ClusterRole");

  @Override
  protected boolean shouldVisitWholeDocument() {
    return true;
  }

  @Override
  protected void registerObjectCheck() {
    register(SENSITIVE_KINDS, document -> document.blocks("rules")
      .filter(CommandExecutionCheck::ruleContainsSensitiveVerb)
      .filter(CommandExecutionCheck::ruleContainsSensitiveResource)
      .forEach(rule -> rule.attribute("resources").reportOnKey(MESSAGE)));
  }

  private static boolean ruleContainsSensitiveVerb(BlockObject rule) {
    return containsSensitiveItemOrWildCard(rule, "verbs", "create");
  }

  private static boolean ruleContainsSensitiveResource(BlockObject rule) {
    return containsSensitiveItemOrWildCard(rule, "resources", "pods/exec");
  }

  private static boolean containsSensitiveItemOrWildCard(BlockObject rule, String listKey, String sensitiveItem) {
    Predicate<YamlTree> verbsPredicate = TreePredicates.isEqualTo(sensitiveItem).or(TreePredicates.isEqualTo("*"));
    return rule.list(listKey).getItemIf(verbsPredicate).findAny().isPresent();
  }
}
