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
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.bicep.IfExpression;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class IfExpressionImpl extends AbstractArmTreeImpl implements IfExpression {

  private final SyntaxToken keyword;
  private final ParenthesizedExpression condition;
  private final ObjectExpression object;

  public IfExpressionImpl(SyntaxToken keyword, ParenthesizedExpression condition, ObjectExpression object) {
    this.keyword = keyword;
    this.condition = condition;
    this.object = object;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, condition, object);
  }

  @Override
  public Kind getKind() {
    return Kind.IF_EXPRESSION;
  }

  @Override
  public Expression conditionValue() {
    return condition.expression();
  }

  @Override
  public ObjectExpression object() {
    return object;
  }
}
