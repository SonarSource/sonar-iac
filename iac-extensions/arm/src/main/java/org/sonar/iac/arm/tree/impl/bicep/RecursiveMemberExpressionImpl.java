/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.FunctionCall;
import org.sonar.iac.arm.tree.api.bicep.RecursiveMemberExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.tree.ArmHelper.addChildrenIfPresent;

public class RecursiveMemberExpressionImpl extends AbstractArmTreeImpl implements RecursiveMemberExpression {

  private final SyntaxToken firstToken;
  @CheckForNull
  private final Expression rightSideExpression;
  @CheckForNull
  private final Identifier rightSideIdentifier;
  @CheckForNull
  private final SyntaxToken secondToken;
  @CheckForNull
  private final RecursiveMemberExpression recursiveMemberExpression;

  public RecursiveMemberExpressionImpl(SyntaxToken firstToken, @Nullable RecursiveMemberExpression recursiveMemberExpression) {
    this.firstToken = firstToken;
    this.rightSideExpression = null;
    this.rightSideIdentifier = null;
    this.secondToken = null;
    this.recursiveMemberExpression = recursiveMemberExpression;
  }

  public RecursiveMemberExpressionImpl(SyntaxToken firstToken, Identifier identifier, @Nullable RecursiveMemberExpression recursiveMemberExpression) {
    this.firstToken = firstToken;
    this.rightSideExpression = null;
    this.rightSideIdentifier = identifier;
    this.secondToken = null;
    this.recursiveMemberExpression = recursiveMemberExpression;
  }

  public RecursiveMemberExpressionImpl(SyntaxToken firstToken, FunctionCall functionCall, @Nullable RecursiveMemberExpression recursiveMemberExpression) {
    this.firstToken = firstToken;
    this.rightSideExpression = functionCall;
    this.rightSideIdentifier = null;
    this.secondToken = null;
    this.recursiveMemberExpression = recursiveMemberExpression;
  }

  public RecursiveMemberExpressionImpl(SyntaxToken firstToken, Expression expression, SyntaxToken secondToken, @Nullable RecursiveMemberExpression recursiveMemberExpression) {
    this.firstToken = firstToken;
    this.rightSideExpression = expression;
    this.rightSideIdentifier = null;
    this.secondToken = secondToken;
    this.recursiveMemberExpression = recursiveMemberExpression;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(firstToken);
    addChildrenIfPresent(result, rightSideExpression);
    addChildrenIfPresent(result, rightSideIdentifier);
    addChildrenIfPresent(result, secondToken);
    addChildrenIfPresent(result, recursiveMemberExpression);
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.RECURSIVE_MEMBER_EXPRESSION;
  }

  @CheckForNull
  @Override
  public Expression rightSideExpression() {
    return rightSideExpression;
  }

  @CheckForNull
  @Override
  public Identifier rightSideIdentifier() {
    return rightSideIdentifier;
  }

  @CheckForNull
  @Override
  public RecursiveMemberExpression recursiveMemberExpression() {
    return recursiveMemberExpression;
  }
}
