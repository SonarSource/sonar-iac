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
package org.sonar.iac.arm.tree.impl.json;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class FunctionCallImpl extends ExpressionImpl implements FunctionCall {
  private final Identifier name;
  private final SeparatedList<Expression, SyntaxToken> argumentList;

  public FunctionCallImpl(
    YamlTreeMetadata metadata,
    Identifier name,
    SeparatedList<Expression, SyntaxToken> argumentList) {
    super(metadata);
    this.name = name;
    this.argumentList = argumentList;
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public SeparatedList<Expression, SyntaxToken> argumentList() {
    return argumentList;
  }

  @Override
  public List<Tree> children() {
    var result = new ArrayList<Tree>();
    result.add(name);
    result.addAll(argumentList.elements());
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.FUNCTION_CALL;
  }
}
