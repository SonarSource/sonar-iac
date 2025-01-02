/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.cloudformation.checks.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.XPathUtils;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class PolicyUtils {

  private PolicyUtils() {
    // utility class
  }

  public static List<Policy> getPolicies(@Nullable Tree root) {
    PolicyCollector collector = new PolicyCollector();
    collector.scan(new TreeContext(), root);
    return collector.policies;
  }

  private static class PolicyCollector extends TreeVisitor<TreeContext> {
    private final List<Policy> policies = new ArrayList<>();

    private PolicyCollector() {
      register(TupleTree.class, (ctx, tree) -> collectPolicy(tree));
    }

    private void collectPolicy(TupleTree tree) {
      if (isPolicyDocument(tree)) {
        YamlTree treeValue = tree.value();
        policies.add(new Policy(treeValue, policy -> XPathUtils.getTrees(policy, "/Statement[]")));
      }
    }

    private static boolean isPolicyDocument(TupleTree tree) {
      return TextUtils.getValue(tree.key()).filter(v -> v.toLowerCase(Locale.ROOT).contains("policy")).isPresent()
        && !XPathUtils.getTrees(tree.value(), "/Statement[]").isEmpty();
    }
  }

}
