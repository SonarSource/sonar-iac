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

import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.arm.tree.api.bicep.WildcardTypeSuffix;

public class WildcardTypeSuffixImpl implements WildcardTypeSuffix {
  private final SyntaxToken dot;
  private final SyntaxToken star;

  public WildcardTypeSuffixImpl(SyntaxToken dot, SyntaxToken star) {
    this.dot = dot;
    this.star = star;
  }

  @Override
  public TypeExpressionAble applyTo(TypeExpressionAble baseType) {
    return new WildcardTypeReferenceImpl(baseType, dot, star);
  }
}
