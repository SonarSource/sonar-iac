/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class FunctionCallTreeImpl extends TerraformTreeImpl implements FunctionCallTree {
  private final SyntaxToken name;
  private final SyntaxToken openParenthesis;
  private final SeparatedTrees<ExpressionTree> arguments;
  private final SyntaxToken closeParenthesis;

  public FunctionCallTreeImpl(SyntaxToken name, SyntaxToken openParenthesis, @Nullable SeparatedTrees<ExpressionTree> arguments, SyntaxToken closeParenthesis) {
    this.name = name;
    this.openParenthesis = openParenthesis;
    this.arguments = arguments != null ? arguments : SeparatedTreesImpl.empty();
    this.closeParenthesis = closeParenthesis;
  }

  @Override
  public SyntaxToken name() {
    return name;
  }

  @Override
  public SeparatedTrees<ExpressionTree> arguments() {
    return arguments;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(Arrays.asList(name, openParenthesis));
    children.addAll(arguments.treesAndSeparators());
    children.add(closeParenthesis);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.FUNCTION_CALL;
  }
}
