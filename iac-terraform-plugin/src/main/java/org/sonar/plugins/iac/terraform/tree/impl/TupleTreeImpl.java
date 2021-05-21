/*
 * SonarQube IaC Terraform Plugin
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
package org.sonar.plugins.iac.terraform.tree.impl;

import org.sonar.plugins.iac.terraform.api.tree.ExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.TupleTree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TupleTreeImpl extends TerraformTree implements TupleTree {
  private final SyntaxToken openBrace;
  private final SeparatedTrees<ExpressionTree> elements;
  private final SyntaxToken closeBrace;

  public TupleTreeImpl(SyntaxToken openBrace, @Nullable SeparatedTrees<ExpressionTree> elements, SyntaxToken closeBrace) {
    this.openBrace = openBrace;
    this.elements = elements != null ? elements : SeparatedTreesImpl.empty();
    this.closeBrace = closeBrace;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(Arrays.asList(openBrace));
    children.addAll(elements.treesAndSeparators());
    children.add(closeBrace);
    return children;
  }

  @Override
  public SeparatedTrees<ExpressionTree> elements() {
    return elements;
  }
}
