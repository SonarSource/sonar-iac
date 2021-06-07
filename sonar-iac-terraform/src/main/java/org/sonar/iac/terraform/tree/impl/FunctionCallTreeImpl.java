/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.terraform.tree.impl;

import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
  public List<TerraformTree> children() {
    List<TerraformTree> children = new ArrayList<>(Arrays.asList(name, openParenthesis));
    children.addAll(arguments.treesAndSeparators());
    children.add(closeParenthesis);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.FUNCTION_CALL;
  }
}
