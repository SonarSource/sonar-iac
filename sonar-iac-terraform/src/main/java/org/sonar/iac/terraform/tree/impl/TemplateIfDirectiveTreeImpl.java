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

import org.sonar.iac.terraform.api.tree.TemplateIfDirectiveTree;
import org.sonar.iac.terraform.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplateIfDirectiveTreeImpl extends TerraformTree implements TemplateIfDirectiveTree {
  private final IfPart ifPart;
  private final ElsePart elsePart;
  private final SyntaxToken endIfOpenToken;
  private final SyntaxToken endIfToken;
  private final SyntaxToken endIfCloseToken;

  public TemplateIfDirectiveTreeImpl(IfPart ifPart, @Nullable ElsePart elsePart, SyntaxToken endIfOpenToken, SyntaxToken endIfToken, SyntaxToken endIfCloseToken) {
    this.ifPart = ifPart;
    this.elsePart = elsePart;
    this.endIfOpenToken = endIfOpenToken;
    this.endIfToken = endIfToken;
    this.endIfCloseToken = endIfCloseToken;
  }

  @Override
  public ExpressionTree condition() {
    return ifPart.condition;
  }

  @Override
  public ExpressionTree trueExpression() {
    return ifPart.trueExpression;
  }

  @Override
  public ExpressionTree falseExpression() {
    if (elsePart == null) {
      return null;
    }

    return elsePart.falseExpression;
  }

  @Override
  public Kind getKind() {
    return Kind.TEMPLATE_DIRECTIVE_IF;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(ifPart.children());
    if (elsePart != null) {
      children.addAll(elsePart.children());
    }
    children.add(endIfOpenToken);
    children.add(endIfToken);
    children.add(endIfCloseToken);

    return children;
  }

  public static class IfPart extends TerraformTree {
    private final SyntaxToken ifOpenToken;
    private final SyntaxToken ifToken;
    private final ExpressionTree condition;
    private final SyntaxToken ifCloseToken;
    private final ExpressionTree trueExpression;

    public IfPart(SyntaxToken ifOpenToken, SyntaxToken ifToken, ExpressionTree condition, SyntaxToken ifCloseToken, ExpressionTree trueExpression) {
      this.ifOpenToken = ifOpenToken;
      this.ifToken = ifToken;
      this.condition = condition;
      this.ifCloseToken = ifCloseToken;
      this.trueExpression = trueExpression;
    }

    @Override
    public Kind getKind() {
      // this will never be used as this is just wrapper class to ease parsing
      return null;
    }

    @Override
    public List<Tree> children() {
      return Arrays.asList(ifOpenToken, ifToken, condition, ifCloseToken, trueExpression);
    }
  }

  public static class ElsePart extends TerraformTree {
    private final SyntaxToken elseOpenToken;
    private final SyntaxToken elseToken;
    private final SyntaxToken elseCloseToken;
    private final ExpressionTree falseExpression;

    public ElsePart(SyntaxToken elseOpenToken, SyntaxToken elseToken, SyntaxToken elseCloseToken, ExpressionTree falseExpression) {
      this.elseOpenToken = elseOpenToken;
      this.elseToken = elseToken;
      this.elseCloseToken = elseCloseToken;
      this.falseExpression = falseExpression;
    }

    @Override
    public Kind getKind() {
      // this will never be used as this is just wrapper class to ease parsing
      return null;
    }

    @Override
    public List<Tree> children() {
      return Arrays.asList(elseOpenToken, elseToken, elseCloseToken, falseExpression);
    }
  }
}
