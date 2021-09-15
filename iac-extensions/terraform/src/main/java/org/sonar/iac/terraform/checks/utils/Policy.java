/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import java.util.Optional;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

public class Policy {

  private final ExpressionTree effect;
  private final ExpressionTree principal;
  private final ExpressionTree action;
  private final ExpressionTree resource;
  private final ExpressionTree condition;

  private Policy() {
    this.effect = null;
    this.principal = null;
    this.action = null;
    this.resource = null;
    this.condition = null;
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
  public static Policy from(Tree policyExpr) {

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

  public boolean isAllowingPolicy() {
    return TextUtils.isValue(effect, "Allow").isTrue();
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
