/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.ForVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ForExpressionImpl extends AbstractArmTreeImpl implements ForExpression {

  private final SyntaxToken leftBracket;
  private final SyntaxToken forKeyword;
  private final ForVariableBlock forVariableBlock;
  private final SyntaxToken inKeyword;
  private final Expression headerExpression;
  private final SyntaxToken colon;
  private final Expression bodyExpression;
  private final SyntaxToken rightBracket;

  // Ignore constructor with 8 parameters, as splitting it doesn't improve readability
  @SuppressWarnings("java:S107")
  public ForExpressionImpl(SyntaxToken leftBracket, SyntaxToken forKeyword, ForVariableBlock forVariableBlock, SyntaxToken inKeyword,
    Expression headerExpression, SyntaxToken colon, Expression bodyExpression, SyntaxToken rightBracket) {
    this.leftBracket = leftBracket;
    this.forKeyword = forKeyword;
    this.forVariableBlock = forVariableBlock;
    this.inKeyword = inKeyword;
    this.headerExpression = headerExpression;
    this.colon = colon;
    this.bodyExpression = bodyExpression;
    this.rightBracket = rightBracket;
  }

  @Override
  public List<Tree> children() {
    return List.of(leftBracket, forKeyword, forVariableBlock, inKeyword, headerExpression, colon, bodyExpression, rightBracket);
  }

  @Override
  public Kind getKind() {
    return Kind.FOR_EXPRESSION;
  }

  @Override
  public ForVariableBlock forVariableBlock() {
    return forVariableBlock;
  }

  @Override
  public Expression headerExpression() {
    return headerExpression;
  }

  @Override
  public Expression bodyExpression() {
    return bodyExpression;
  }

  @Override
  public SyntaxToken forKeyword() {
    return forKeyword;
  }

  @Override
  public SyntaxToken inKeyword() {
    return inKeyword;
  }
}
