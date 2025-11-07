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
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.arm.tree.api.bicep.WildcardTypeReference;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class WildcardTypeReferenceImpl extends AbstractArmTreeImpl implements WildcardTypeReference {
  private final TypeExpressionAble type;
  private final SyntaxToken dot;
  private final SyntaxToken star;

  public WildcardTypeReferenceImpl(TypeExpressionAble type, SyntaxToken dot, SyntaxToken star) {
    this.type = type;
    this.dot = dot;
    this.star = star;
  }

  @Override
  public TypeExpressionAble getType() {
    return type;
  }

  @Override
  public List<Tree> children() {
    return List.of(type, dot, star);
  }

  @Override
  public Kind getKind() {
    return Kind.WILDCARD_TYPE_REFERENCE;
  }

  @Override
  public String toString() {
    var result = type.toString();
    result += dot.toString();
    result += star.toString();
    return result;
  }
}
