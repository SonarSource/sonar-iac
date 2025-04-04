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
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.HasDecorators;
import org.sonar.iac.arm.tree.api.bicep.HasKeyword;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.tree.ArmHelper.addChildrenIfPresent;

public class OutputDeclarationImpl extends AbstractArmTreeImpl implements OutputDeclaration, HasDecorators, HasKeyword {

  private final List<Decorator> decorators;
  private final SyntaxToken keyword;
  private final Identifier name;
  @Nullable
  private final SingularTypeExpression identifierType;
  @Nullable
  private final SyntaxToken resource;
  @Nullable
  private final InterpolatedString interpType;
  private final SyntaxToken equ;
  private final Expression expression;

  public OutputDeclarationImpl(List<Decorator> decorators, SyntaxToken keyword, Identifier name, SingularTypeExpression identifierType, SyntaxToken equ, Expression expression) {
    this.decorators = decorators;
    this.keyword = keyword;
    this.name = name;
    this.identifierType = identifierType;
    this.equ = equ;
    this.expression = expression;
    this.resource = null;
    this.interpType = null;
  }

  public OutputDeclarationImpl(
    List<Decorator> decorators,
    SyntaxToken keyword,
    Identifier name,
    SyntaxToken resource,
    InterpolatedString interpType,
    SyntaxToken equ,
    Expression expression) {
    this.decorators = decorators;
    this.keyword = keyword;
    this.name = name;
    this.resource = resource;
    this.interpType = interpType;
    this.equ = equ;
    this.expression = expression;
    this.identifierType = null;
  }

  @Override
  public Identifier declaratedName() {
    return name;
  }

  @Override
  public ArmTree type() {
    if (identifierType != null) {
      return identifierType;
    } else {
      return interpType;
    }
  }

  @CheckForNull
  @Override
  public StringLiteral condition() {
    // there is no possibility in Bicep to have a conditional output, only conditional value which get embedded in the expression
    return null;
  }

  @CheckForNull
  @Override
  public StringLiteral copyCount() {
    // The copy features in JSON ARM is the equivalent of an iterative output, which in Bicep is represented directly in the Expression as a
    // ForExpression
    return null;
  }

  @CheckForNull
  @Override
  public Expression copyInput() {
    // Same as above for copyCount
    return null;
  }

  @CheckForNull
  @Override
  public Expression value() {
    return expression;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(decorators);
    children.add(keyword);
    children.add(name);
    addChildrenIfPresent(children, identifierType);
    addChildrenIfPresent(children, resource);
    addChildrenIfPresent(children, interpType);
    children.add(equ);
    children.add(expression);
    return children;
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
