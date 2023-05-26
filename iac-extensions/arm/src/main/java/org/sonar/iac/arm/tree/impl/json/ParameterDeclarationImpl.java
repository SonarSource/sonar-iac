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
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ParameterDeclarationImpl extends AbstractArmTreeImpl implements ParameterDeclaration {

  private final Identifier identifier;
  private final Property type;
  private final Expression defaultValue;
  private final List<Expression> allowedValues;
  private final Expression description;
  private final Expression minValue;
  private final Expression maxValue;
  private final Expression minLength;
  private final Expression maxLength;

  // Methods should not have too many parameters
  @SuppressWarnings("java:S107")
  public ParameterDeclarationImpl(
    Identifier identifier,
    Property type,
    @Nullable Expression defaultValue,
    @Nullable List<Expression> allowedValues,
    @Nullable Expression description,
    @Nullable Expression minValue,
    @Nullable Expression maxValue,
    @Nullable Expression minLength,
    @Nullable Expression maxLength) {

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
    children.add(type.key());
    children.add(type.value());
    if (defaultValue != null) {
      children.add(defaultValue);
    }
    children.addAll(allowedValues);
    if (description != null) {
      children.add(description);
    }
    if (minValue != null) {
      children.add(minValue);
    }
    if (maxValue != null) {
      children.add(maxValue);
    }
    if (minLength != null) {
      children.add(minLength);
    }
    if (maxLength != null) {
      children.add(maxLength);
    }
    return children;
  }

  @Override
  public Identifier identifier() {
    return identifier;
  }

  @Override
  public ParameterType type() {
    return ParameterType.fromName(type.value().value());
  }

  @Override
  @CheckForNull
  public Expression defaultValue() {
    return defaultValue;
  }

  @Override
  public List<Expression> allowedValues() {
    return allowedValues;
  }

  @Override
  @CheckForNull
  public Expression description() {
    return description;
  }

  @Override
  @CheckForNull
  public Expression minValue() {
    return minValue;
  }

  @Override
  @CheckForNull
  public Expression maxValue() {
    return maxValue;
  }

  @Override
  @CheckForNull
  public Expression minLength() {
    return minLength;
  }

  @Override
  @CheckForNull
  public Expression maxLength() {
    return maxLength;
  }

  @Override
  public Kind getKind() {
    return Kind.PARAMETER_DECLARATION;
  }
}
