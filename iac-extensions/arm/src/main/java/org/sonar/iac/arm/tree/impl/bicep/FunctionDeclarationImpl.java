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
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.FunctionDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypedLambdaExpression;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class FunctionDeclarationImpl extends AbstractArmTreeImpl implements FunctionDeclaration {

  private final List<Decorator> decorators;
  private final SyntaxToken keyword;
  private final Identifier name;
  private final TypedLambdaExpression lambdaExpression;

  public FunctionDeclarationImpl(List<Decorator> decorators, SyntaxToken keyword, Identifier name, TypedLambdaExpression lambdaExpression) {
    this.decorators = decorators;
    this.keyword = keyword;
    this.name = name;
    this.lambdaExpression = lambdaExpression;
  }

  @Override
  public Identifier declaratedName() {
    return name;
  }

  @Override
  public TypedLambdaExpression lambdaExpression() {
    return lambdaExpression;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(decorators);
    children.add(keyword);
    children.add(name);
    children.add(lambdaExpression);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.FUNCTION_DECLARATION;
  }

  @Override
  public List<Decorator> decorators() {
    return decorators;
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }
}
