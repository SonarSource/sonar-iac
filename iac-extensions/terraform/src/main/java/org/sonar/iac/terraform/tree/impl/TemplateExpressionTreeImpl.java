/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TemplateExpressionTree;

public class TemplateExpressionTreeImpl extends TerraformTreeImpl implements TemplateExpressionTree {

  private final SyntaxToken openQuote;
  private final List<ExpressionTree> parts;
  private final SyntaxToken closeQuote;

  public TemplateExpressionTreeImpl(SyntaxToken openQuote, List<ExpressionTree> parts, SyntaxToken closeQuote) {
    this.openQuote = openQuote;
    this.parts = parts;
    this.closeQuote = closeQuote;
  }

  @Override
  public List<ExpressionTree> parts() {
    return parts;
  }

  @Override
  public Kind getKind() {
    return Kind.TEMPLATE_EXPRESSION;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openQuote);
    children.addAll(parts);
    children.add(closeQuote);
    return children;
  }
}
