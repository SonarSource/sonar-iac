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
package org.sonar.iac.arm.tree.impl.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.tree.impl.json.ArmHelper.addChildrenIfPresent;

public class ParameterDeclarationImpl extends AbstractArmTreeImpl implements ParameterDeclaration {

  private final Identifier identifier;
  private final StringLiteral type;
  private final Expression defaultValue;
  private final ArrayExpression allowedValues;
  private final StringLiteral description;
  private final NumericLiteral minValue;
  private final NumericLiteral maxValue;
  private final NumericLiteral minLength;
  private final NumericLiteral maxLength;

  // Methods should not have too many parameters
  @SuppressWarnings("java:S107")
  public ParameterDeclarationImpl(
    Identifier identifier,
    StringLiteral type,
    @Nullable Expression defaultValue,
    @Nullable ArrayExpression allowedValues,
    @Nullable StringLiteral description,
    @Nullable NumericLiteral minValue,
    @Nullable NumericLiteral maxValue,
    @Nullable NumericLiteral minLength,
    @Nullable NumericLiteral maxLength) {

    this.identifier = identifier;
    this.type = type;
    this.defaultValue = defaultValue;
    this.allowedValues = allowedValues;
    this.description = description;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(identifier);
    children.add(type);
    addChildrenIfPresent(children, defaultValue);
    addChildrenIfPresent(children, description);
    addChildrenIfPresent(children, minValue);
    addChildrenIfPresent(children, maxValue);
    addChildrenIfPresent(children, minLength);
    addChildrenIfPresent(children, maxLength);
    addChildrenIfPresent(children, allowedValues);
    return children;
  }

  @Override
  public Identifier identifier() {
    return identifier;
  }

  @Override
  public ParameterType type() {
    return ParameterType.fromName(type.value());
  }

  @Override
  @CheckForNull
  public Expression defaultValue() {
    return defaultValue;
  }

  @Override
  public List<Expression> allowedValues() {
    return Optional.ofNullable(allowedValues).map(ArrayExpression::elements).orElse(Collections.emptyList());
  }

  @Override
  @CheckForNull
  public StringLiteral description() {
    return description;
  }

  @Override
  @CheckForNull
  public NumericLiteral minValue() {
    return minValue;
  }

  @Override
  @CheckForNull
  public NumericLiteral maxValue() {
    return maxValue;
  }

  @Override
  @CheckForNull
  public NumericLiteral minLength() {
    return minLength;
  }

  @Override
  @CheckForNull
  public NumericLiteral maxLength() {
    return maxLength;
  }

  @Override
  public Kind getKind() {
    return Kind.PARAMETER_DECLARATION;
  }
}
