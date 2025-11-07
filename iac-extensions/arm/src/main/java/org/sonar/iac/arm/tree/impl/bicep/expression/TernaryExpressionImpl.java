/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.impl.bicep.expression;

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.expression.TernaryExpression;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class TernaryExpressionImpl extends AbstractArmTreeImpl implements TernaryExpression {

  private final Expression condition;
  private final SyntaxToken query;
  private final Expression ifTrueExpression;
  private final SyntaxToken colon;
  private final Expression elseExpression;

  public TernaryExpressionImpl(Expression condition, SyntaxToken query, Expression ifTrueExpression, SyntaxToken colon, Expression elseExpression) {
    this.condition = condition;
    this.query = query;
    this.ifTrueExpression = ifTrueExpression;
    this.colon = colon;
    this.elseExpression = elseExpression;
  }

  @Override
  public Expression condition() {
    return condition;
  }

  @Override
  public Expression ifTrueExpression() {
    return ifTrueExpression;
  }

  @Override
  public Expression elseExpression() {
    return elseExpression;
  }

  @Override
  public List<Tree> children() {
    return List.of(condition, query, ifTrueExpression, colon, elseExpression);
  }
}
