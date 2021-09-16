/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

public class PolicyUtils {

  private PolicyUtils() {
    // Utility class
  }

  public static final Policy UNKNOWN_POLCY = new Policy(null, t -> Collections.emptyList());

  public static List<Policy> getPolicies(Tree root) {
    PolicyCollector collector = new PolicyCollector();
    collector.scan(new TreeContext(), root);
    return collector.policies;
  }

  private static class PolicyCollector extends TreeVisitor<TreeContext> {
    private final List<Policy> policies = new ArrayList<>();

    private PolicyCollector() {
      register(BlockTree.class, (ctx, tree) -> collectPolicy(tree));
    }

    /**
     * Attempt to create a policy instance to reason about out of a structure like the following:
     *
     * jsonencode({
     *     Version = "2012-10-17"
     *     Id      = "somePolicy"
     *     Statement = [
     *       {
     *         Sid       = "HTTPSOnly"
     *         Effect    = "Deny"
     *         Principal = "*"
     *         Action    = "s3:*"
     *         Resource = ["someResource"]
     *         Condition = { Bool = { "aws:SecureTransport" = "false" } }
     *       },
     *     ]
     * })
     *
     * In case the policy tree does not have the expected structure (e.g., is provided as a heredoc), we create an incomplete policy
     * which we consider as safe as we cannot reason about it.
     */
    private void collectPolicy(BlockTree tree) {
      Optional<Tree> policyDocument = findPolicyDocument(tree);
      if (policyDocument.isPresent()) {
        Tree policyExpr = policyDocument.get();
        // For now we only handle policy expressions if they are wrapped by a function call
        if (!(policyExpr instanceof FunctionCallTree) || ((FunctionCallTree) policyExpr).arguments().trees().isEmpty()) {
          policies.add(UNKNOWN_POLCY);
          return;
        }
        TerraformTree policyArgument = ((FunctionCallTree) policyExpr).arguments().trees().get(0);
        policies.add(new Policy(
          policyArgument,
          policy -> PropertyUtils.value(policy, "Statement", TupleTree.class)
            .map(TupleTree::elements)
            .map(SeparatedTrees::treesAndSeparators)
            .orElse(Collections.emptyList())));
      }
    }

    private static Optional<Tree> findPolicyDocument(BlockTree tree) {
      return PropertyUtils.value(tree, key -> key.contains("policy"));
    }
  }
}
