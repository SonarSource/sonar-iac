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

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypedLambdaExpression;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedVariableBlock;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class TypedLambdaExpressionImpl extends AbstractArmTreeImpl implements TypedLambdaExpression {
  private final TypedVariableBlock typedVariableBlock;
  private final AmbientTypeReference primaryTypeExpression;
  private final SyntaxToken doubleArrow;
  private final Expression expression;

  public TypedLambdaExpressionImpl(TypedVariableBlock typedVariableBlock, AmbientTypeReference primaryTypeExpression, SyntaxToken doubleArrow, Expression expression) {
    this.typedVariableBlock = typedVariableBlock;
    this.primaryTypeExpression = primaryTypeExpression;
    this.doubleArrow = doubleArrow;
    this.expression = expression;
  }

  @Override
  public List<Tree> children() {
    return List.of(typedVariableBlock, primaryTypeExpression, doubleArrow, expression);
  }
}
