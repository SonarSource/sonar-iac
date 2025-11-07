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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ParenthesizedTypeExpressionImpl extends AbstractArmTreeImpl implements ParenthesizedTypeExpression {
  private final SyntaxToken openingParenthesis;
  private final TypeExpressionAble typeExpression;
  private final SyntaxToken closingParenthesis;

  public ParenthesizedTypeExpressionImpl(SyntaxToken openingParenthesis, TypeExpressionAble typeExpression, SyntaxToken closingParenthesis) {
    this.openingParenthesis = openingParenthesis;
    this.typeExpression = typeExpression;
    this.closingParenthesis = closingParenthesis;
  }

  @Override
  public TypeExpressionAble typeExpression() {
    return typeExpression;
  }

  @Override
  public List<Tree> children() {
    return List.of(openingParenthesis, typeExpression, closingParenthesis);
  }
}
