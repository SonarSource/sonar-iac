/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Optional;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

public abstract class AbstractResourceCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, (ctx, tree) -> {
      if (isResource(tree)) {
        checkResource(ctx, tree);
      }
    });
  }
  protected abstract void checkResource(CheckContext ctx, BlockTree resource);

  public static boolean isResource(BlockTree tree) {
    return TextUtils.isValue(tree.key(), "resource").isTrue();
  }

  public static boolean isResource(BlockTree tree, String type) {
    return isResource(tree) && !tree.labels().isEmpty() && type.equals(tree.labels().get(0).value());
  }

  public static boolean isS3Bucket(BlockTree tree) {
    return !tree.labels().isEmpty() && "aws_s3_bucket".equals(tree.labels().get(0).value());
  }

  public static boolean isS3BucketResource(BlockTree tree) {
    return isResource(tree, "aws_s3_bucket");
  }

  protected static class Policy {
    private ExpressionTree effect;
    private ExpressionTree principal;
    private ExpressionTree action;
    private ExpressionTree resource;
    private ExpressionTree condition;

    private Policy() {
    }

    private Policy(ObjectTree statement) {
      this.effect = PropertyUtils.valueOrNull(statement, "Effect", ExpressionTree.class);
      this.principal = PropertyUtils.valueOrNull(statement, "Principal", ExpressionTree.class);
      this.action = PropertyUtils.valueOrNull(statement, "Action", ExpressionTree.class);
      this.resource = PropertyUtils.valueOrNull(statement, "Resource", ExpressionTree.class);
      this.condition = PropertyUtils.valueOrNull(statement, "Condition", ExpressionTree.class);
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
    protected static Policy from(Tree policyExpr) {

      // For now we only handle policy expressions if they are wrapped by a function call
      if (!(policyExpr instanceof FunctionCallTree) || ((FunctionCallTree) policyExpr).arguments().trees().isEmpty()) {
        return new Policy();
      }

      ExpressionTree policyArgument = ((FunctionCallTree) policyExpr).arguments().trees().get(0);
      Optional<ExpressionTree> statementField = PropertyUtils.value(policyArgument, "Statement", ExpressionTree.class);

      if (!statementField.isPresent() || !(statementField.get() instanceof TupleTree) ||
        ((TupleTree) statementField.get()).elements().trees().isEmpty() ||
        !(((TupleTree) statementField.get()).elements().trees().get(0) instanceof ObjectTree)) {
        return new Policy();
      }

      ObjectTree policyStatement = (ObjectTree) ((TupleTree) statementField.get()).elements().trees().get(0);
      return new Policy(policyStatement);
    }

    public Optional<ExpressionTree> effect() {
      return Optional.ofNullable(effect);
    }

    public Optional<ExpressionTree> principal() {
      return Optional.ofNullable(principal);
    }

    public Optional<ExpressionTree> action() {
      return Optional.ofNullable(action);
    }

    public Optional<ExpressionTree> resource() {
      return Optional.ofNullable(resource);
    }

    public Optional<ExpressionTree> condition() {
      return Optional.ofNullable(condition);
    }
  }
}
