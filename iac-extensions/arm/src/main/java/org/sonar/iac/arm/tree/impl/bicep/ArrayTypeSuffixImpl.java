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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.bicep.ArrayTypeSuffix;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;

public class ArrayTypeSuffixImpl implements ArrayTypeSuffix {
  private final SyntaxToken lBracket;
  @CheckForNull
  private final NumericLiteral length;
  private final SyntaxToken rBracket;

  public ArrayTypeSuffixImpl(SyntaxToken lBracket, @Nullable NumericLiteral length, SyntaxToken rBracket) {
    this.lBracket = lBracket;
    this.length = length;
    this.rBracket = rBracket;
  }

  @Override
  public TypeExpressionAble buildType(TypeExpressionAble type) {
    return new ArrayTypeReferenceImpl(type, lBracket, length, rBracket);
  }
}
