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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.bicep.ArrayTypeSuffix;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.arm.tree.api.bicep.TypeReferenceSuffix;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

public class ArrayTypeSuffixImpl extends AbstractArmTreeImpl implements ArrayTypeSuffix {
  private TypeExpressionAble type;
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
  public void setType(TypeExpressionAble type) {
    if (this.type == null) {
      this.type = type;
    } else {
      ((TypeReferenceSuffix) this.type).setType(type);
    }
  }

  @Override
  public String value() {
    String value = ((TextTree) type).value() + lBracket.value();
    if (length != null) {
      value += length.value();
    }
    return value + rBracket.value();
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(type.children());
    children.add(lBracket);
    if (length != null) {
      children.add(length);
    }
    children.add(rBracket);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_TYPE_SUFFIX;
  }

  @Override
  public String toString() {
    String result = "";
    if (type != null) {
      result += type.toString();
    }
    result += lBracket.toString();
    if (length != null) {
      result += length.toString();
    }
    result += rBracket.toString();
    return result;
  }
}
