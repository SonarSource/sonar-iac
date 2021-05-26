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
import org.sonar.plugins.iac.terraform.api.tree.IndexAccessExprTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;

import java.util.Arrays;
import java.util.List;

public class IndexAccessExprTreeImpl extends TerraformTree implements IndexAccessExprTree {
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
  public Kind getKind() {
    return Kind.INDEX_ACCESS_EXPR;
  }
}
