/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.arm.tree.impl.bicep;

import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.bicep.ArrayTypeSuffix;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;

public class ArrayTypeSuffixImpl implements ArrayTypeSuffix {
  private final SyntaxToken lBracket;
  @Nullable
  private final NumericLiteral index;
  @Nullable
  private final SyntaxToken star;
  private final SyntaxToken rBracket;

  public ArrayTypeSuffixImpl(SyntaxToken lBracket, @Nullable NumericLiteral index, @Nullable SyntaxToken star, SyntaxToken rBracket) {
    this.lBracket = lBracket;
    this.index = index;
    this.star = star;
    this.rBracket = rBracket;
  }

  @Override
  public TypeExpressionAble applyTo(TypeExpressionAble baseType) {
    return new ArrayTypeReferenceImpl(baseType, lBracket, index, star, rBracket);
  }
}
