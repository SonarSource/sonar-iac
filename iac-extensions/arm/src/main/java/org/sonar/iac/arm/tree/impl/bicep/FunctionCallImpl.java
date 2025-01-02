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
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public class FunctionCallImpl extends AbstractArmTreeImpl implements FunctionCall {

  private final Identifier identifier;
  private final SyntaxToken leftParenthesis;
  private final SeparatedList<Expression, SyntaxToken> argumentList;
  private final SyntaxToken rightParenthesis;

  public FunctionCallImpl(Identifier identifier, SyntaxToken leftParenthesis, SeparatedList<Expression, SyntaxToken> argumentList, SyntaxToken rightParenthesis) {
    this.identifier = identifier;
    this.leftParenthesis = leftParenthesis;
    this.argumentList = argumentList;
    this.rightParenthesis = rightParenthesis;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(identifier);
    result.add(leftParenthesis);
    result.addAll(argumentList.elementsAndSeparators());
    result.add(rightParenthesis);
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.FUNCTION_CALL;
  }

  @Override
  public Identifier name() {
    return identifier;
  }

  @Override
  public SeparatedList<Expression, SyntaxToken> argumentList() {
    return argumentList;
  }
}
