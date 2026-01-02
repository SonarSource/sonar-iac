/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.bicep.ArrayTypeReference;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.tree.ArmHelper.addChildrenIfPresent;

public class ArrayTypeReferenceImpl extends AbstractArmTreeImpl implements ArrayTypeReference {
  private final TypeExpressionAble type;
  private final SyntaxToken lBracket;
  @Nullable
  private final NumericLiteral index;
  @Nullable
  private final SyntaxToken star;
  private final SyntaxToken rBracket;

  public ArrayTypeReferenceImpl(TypeExpressionAble type, SyntaxToken lBracket, @Nullable NumericLiteral index, @Nullable SyntaxToken star, SyntaxToken rBracket) {
    this.type = type;
    this.lBracket = lBracket;
    this.index = index;
    this.star = star;
    this.rBracket = rBracket;
  }

  @Override
  public TypeExpressionAble getType() {
    return type;
  }

  @CheckForNull
  @Override
  public NumericLiteral getIndex() {
    return index;
  }

  @CheckForNull
  @Override
  public SyntaxToken getStar() {
    return star;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(type);
    children.add(lBracket);
    addChildrenIfPresent(children, index);
    addChildrenIfPresent(children, star);
    children.add(rBracket);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_TYPE_REFERENCE;
  }

  @Override
  public String toString() {
    var result = type.toString();
    result += lBracket.toString();
    if (index != null) {
      result += index.toString();
    }
    result += rBracket.toString();
    return result;
  }
}
