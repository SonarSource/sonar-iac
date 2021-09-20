/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

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
        CloudformationTree treeValue = tree.value();
        policies.add(new Policy(treeValue, policy -> XPathUtils.getTrees(policy, "/Statement[]")));
      }
    }

    private static boolean isPolicyDocument(TupleTree tree) {
      return TextUtils.getValue(tree.key()).filter(v -> v.toLowerCase(Locale.ROOT).contains("policy")).isPresent() 
        && !XPathUtils.getTrees(tree.value(), "/Statement[]").isEmpty();
    }
  }

}
