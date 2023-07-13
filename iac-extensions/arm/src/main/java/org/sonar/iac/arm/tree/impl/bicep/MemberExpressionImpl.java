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
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.tree.ArmHelper.addChildrenIfPresent;

public class MemberExpressionImpl extends AbstractArmTreeImpl implements MemberExpression {

  private final SyntaxToken firstToken;
  @CheckForNull
  private final Expression expression;
  @CheckForNull
  private final SyntaxToken secondToken;

  private Expression referencingObject;

  public MemberExpressionImpl(SyntaxToken firstToken) {
    this.firstToken = firstToken;
    this.expression = null;
    this.secondToken = null;
  }

  public MemberExpressionImpl(SyntaxToken firstToken, Expression expression) {
    this.firstToken = firstToken;
    this.expression = expression;
    this.secondToken = null;
  }

  public MemberExpressionImpl(SyntaxToken firstToken, Expression expression, SyntaxToken secondToken) {
    this.firstToken = firstToken;
    this.expression = expression;
    this.secondToken = secondToken;
  }

  public MemberExpression complete(Expression object) {
    this.referencingObject = object;
    return this;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(referencingObject);
    result.add(firstToken);
    addChildrenIfPresent(result, expression);
    addChildrenIfPresent(result, secondToken);
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.MEMBER_EXPRESSION;
  }

  @CheckForNull
  @Override
  public Expression expression() {
    return expression;
  }

  @Override
  public Expression referencingObject() {
    return referencingObject;
  }

}
