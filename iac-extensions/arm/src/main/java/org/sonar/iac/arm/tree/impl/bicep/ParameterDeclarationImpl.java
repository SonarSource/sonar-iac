/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.HasDecorators;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.tree.ArmHelper.addChildrenIfPresent;

public class ParameterDeclarationImpl extends AbstractArmTreeImpl implements ParameterDeclaration, HasDecorators {

  private final List<Decorator> decorators;
  private final SyntaxToken keyword;
  private final Identifier name;
  @Nullable
  private final StringLiteral typeExpression;
  @Nullable
  private final SyntaxToken resource;
  @Nullable
  private final InterpolatedString typeInterp;
  @Nullable
  private final SyntaxToken equ;
  @Nullable
  private final Expression defaultValue;

  public ParameterDeclarationImpl(
    List<Decorator> decorators,
    SyntaxToken keyword,
    Identifier name,
    @Nullable StringLiteral typeExpression,
    @Nullable SyntaxToken equ,
    @Nullable Expression defaultValue) {
    this.decorators = decorators;
    this.keyword = keyword;
    this.name = name;
    this.typeExpression = typeExpression;
    this.equ = equ;
    this.defaultValue = defaultValue;
    this.resource = null;
    this.typeInterp = null;
  }

  public ParameterDeclarationImpl(List<Decorator> decorators,
    SyntaxToken keyword,
    Identifier name,
    @Nullable SyntaxToken resource,
    @Nullable InterpolatedString typeInterp,
    @Nullable SyntaxToken equ,
    @Nullable Expression defaultValue) {
    this.decorators = decorators;
    this.keyword = keyword;
    this.name = name;
    this.resource = resource;
    this.typeInterp = typeInterp;
    this.equ = equ;
    this.defaultValue = defaultValue;
    this.typeExpression = null;
  }

  @Override
  public Identifier identifier() {
    return name;
  }

  @Override
  @CheckForNull
  public ParameterType type() {
    if (typeExpression != null) {
      // TODO SONARIAC-963 Put in place typeExpression
      return ParameterType.fromName(typeExpression.value());
    }
    return null;
  }

  @CheckForNull
  @Override
  public TextTree resourceType() {
    return typeInterp;
  }

  @Override
  @CheckForNull
  public Expression defaultValue() {
    return defaultValue;
  }

  @Override
  public List<Expression> allowedValues() {
    return findDecoratorByName("allowed").map(Decorator::functionCallOrMemberFunctionCall).map(
      d -> ((ArrayExpression) (d.argumentList().elements().get(0))).elements()).orElse(Collections.emptyList());
  }

  @Override
  @CheckForNull
  public StringLiteral description() {
    return findDecoratorByName("description")
      .map(Decorator::functionCallOrMemberFunctionCall)
      .map(d -> (d.argumentList().elements().get(0)))
      .map(s -> ((StringComplete) s).content())
      .orElse(null);
  }

  @Override
  @CheckForNull
  public NumericLiteral minValue() {
    return findDecoratorByName("minValue").map(Decorator::functionCallOrMemberFunctionCall).map(
      d -> ((NumericLiteral) (d.argumentList().elements().get(0)))).orElse(null);
  }

  @Override
  @CheckForNull
  public NumericLiteral maxValue() {
    return findDecoratorByName("maxValue").map(Decorator::functionCallOrMemberFunctionCall).map(
      d -> ((NumericLiteral) (d.argumentList().elements().get(0)))).orElse(null);
  }

  @Override
  @CheckForNull
  public NumericLiteral minLength() {
    return findDecoratorByName("minLength").map(Decorator::functionCallOrMemberFunctionCall).map(
      d -> ((NumericLiteral) (d.argumentList().elements().get(0)))).orElse(null);
  }

  @Override
  @CheckForNull
  public NumericLiteral maxLength() {
    return findDecoratorByName("maxLength").map(Decorator::functionCallOrMemberFunctionCall).map(
      d -> ((NumericLiteral) (d.argumentList().elements().get(0)))).orElse(null);
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(decorators);
    children.add(keyword);
    children.add(name);
    addChildrenIfPresent(children, typeExpression);
    addChildrenIfPresent(children, resource);
    addChildrenIfPresent(children, typeInterp);
    addChildrenIfPresent(children, equ);
    addChildrenIfPresent(children, defaultValue);
    return children;
  }

  @Override
  public List<Decorator> decorators() {
    return decorators;
  }
}
