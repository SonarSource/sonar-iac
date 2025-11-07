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
package org.sonar.iac.arm.tree.impl.bicep.variable;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.variable.LocalVariable;
import org.sonar.iac.arm.tree.api.bicep.variable.VariableBlock;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public class VariableBlockImpl extends AbstractArmTreeImpl implements VariableBlock {
  private final SyntaxToken lPar;
  private final SeparatedList<LocalVariable, SyntaxToken> variableList;
  private final SyntaxToken rPar;

  public VariableBlockImpl(SyntaxToken lPar, SeparatedList<LocalVariable, SyntaxToken> variableList, SyntaxToken rPar) {
    this.lPar = lPar;
    this.variableList = variableList;
    this.rPar = rPar;
  }

  @Override
  public List<LocalVariable> variables() {
    return variableList.elements();
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(lPar);
    children.addAll(variableList.elementsAndSeparators());
    children.add(rPar);
    return children;
  }
}
