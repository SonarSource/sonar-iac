/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.IndexAccessExprTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;

public class IndexAccessExprTreeImpl extends TerraformTreeImpl implements IndexAccessExprTree {
  private final ExpressionTree subject;
  private final ExpressionTree index;
  private final SyntaxToken closeBracket;
  private final SyntaxToken openBracket;

  public IndexAccessExprTreeImpl(ExpressionTree subject, SyntaxToken openBracket, ExpressionTree index, SyntaxToken closeBracket) {
    this.subject = subject;
    this.openBracket = openBracket;
    this.index = index;
    this.closeBracket = closeBracket;
  }

  @Override
  public ExpressionTree subject() {
    return subject;
  }

  @Override
  public ExpressionTree index() {
    return index;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(subject, openBracket, index, closeBracket);
  }

  @Override
  public TerraformTree.Kind getKind() {
    return TerraformTree.Kind.INDEX_ACCESS_EXPR;
  }
}
