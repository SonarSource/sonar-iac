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

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedLocalVariable;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedVariableBlock;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public class TypedVariableBlockImpl extends AbstractArmTreeImpl implements TypedVariableBlock {
  private final SyntaxToken lParen;
  private final SeparatedList<TypedLocalVariable, SyntaxToken> typedVariableList;
  private final SyntaxToken rParen;

  public TypedVariableBlockImpl(SyntaxToken lParen, SeparatedList<TypedLocalVariable, SyntaxToken> typedVariableList, SyntaxToken rParen) {
    this.lParen = lParen;
    this.typedVariableList = typedVariableList;
    this.rParen = rParen;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(lParen);
    children.addAll(typedVariableList.elementsAndSeparators());
    children.add(rParen);
    return children;
  }
}
