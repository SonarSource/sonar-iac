/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.checks.policy.Policy.Statement;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.HeredocLiteralTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

public class PolicyUtils {

  private PolicyUtils() {
    // Utility class
  }

  public static final Policy UNKNOWN_POLICY = new Policy(null, null, Collections.emptyList());

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
      if (policyDocument.isEmpty()) {
        return;
      }
      Tree policyExpr = policyDocument.get();
      TerraformTree policyRoot = policyRoot(policyExpr);
      if (policyRoot == null) {
        // Unknown shape (file(...), data references, raw string literal, non-JSON heredoc, etc.) — fail safe
        policies.add(UNKNOWN_POLICY);
        return;
      }
      List<Statement> statements = PropertyUtils.value(policyRoot, "Statement", TupleTree.class)
        .map(TupleTree::elements)
        .map(SeparatedTrees::trees)
        .orElse(Collections.emptyList())
        .stream().map(Statement::new).toList();
      policies.add(new Policy(
        PropertyUtils.valueOrNull(policyRoot, "Version"),
        PropertyUtils.valueOrNull(policyRoot, "Id"),
        statements));
    }

    /**
     * Resolve the root object expression of a policy attribute. Both {@code jsonencode({...})} and
     * heredoc-with-JSON now expose an {@code ObjectTree} at the top — the former via the parser
     * (HCL native object literal), the latter via {@link HeredocLiteralTree#content()} produced at
     * parse time. Returns {@code null} when the attribute does not resolve to a structured object.
     */
    private static TerraformTree policyRoot(Tree policyExpr) {
      if (policyExpr instanceof FunctionCallTree policyFunctionCall && !policyFunctionCall.arguments().trees().isEmpty()) {
        return policyFunctionCall.arguments().trees().get(0);
      }
      if (policyExpr instanceof HeredocLiteralTree heredoc && heredoc.content() instanceof ObjectTree object) {
        return object;
      }
      return null;
    }

    private static Optional<Tree> findPolicyDocument(BlockTree tree) {
      return PropertyUtils.value(tree, key -> key.contains("policy"));
    }
  }
}
