/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.sonar.iac.arm.tree.api.bicep.UnaryOperator;
import org.sonar.iac.arm.tree.api.bicep.expression.UnaryExpression;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class UnaryExpressionImpl extends AbstractArmTreeImpl implements UnaryExpression {
  private final UnaryOperator operator;
  private final Expression expression;

  public UnaryExpressionImpl(UnaryOperator operator, Expression expression) {
    this.operator = operator;
    this.expression = expression;
  }

  @Override
  public UnaryOperator operator() {
    return operator;
  }

  @Override
  public Expression expression() {
    return expression;
  }

  @Override
  public List<Tree> children() {
    return List.of(operator, expression);
  }
}
