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
package org.sonar.iac.arm.tree.impl.bicep.expression;

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.expression.BinaryOperation;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public abstract class AbstractBinaryOperation extends AbstractArmTreeImpl implements BinaryOperation {

  private final SeparatedList<Expression, SyntaxToken> separatedList;

  protected AbstractBinaryOperation(SeparatedList<Expression, SyntaxToken> separatedList) {
    this.separatedList = separatedList;
  }

  @Override
  public SeparatedList<Expression, SyntaxToken> separatedList() {
    return separatedList;
  }

  @Override
  public List<Tree> children() {
    return separatedList.elementsAndSeparators();
  }
}
