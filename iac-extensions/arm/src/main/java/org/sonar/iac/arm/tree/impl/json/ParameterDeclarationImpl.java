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

import static java.util.Collections.emptyList;

public class ParameterDeclarationImpl extends AbstractArmTreeImpl implements ParameterDeclaration {

  private final Identifier identifier;
  private final Property type;
  private Expression defaultValue;
  private List<Expression> allowedValues = emptyList();
  private Expression description;
  private Expression minValue;
  private Expression maxValue;
  private Expression minLength;
  private Expression maxLength;

  public ParameterDeclarationImpl(Identifier identifier, Property type) {
    this.identifier = identifier;
    this.type = type;
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

  public void setDefaultValue(@Nullable Expression defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public List<Expression> allowedValues() {
    return allowedValues;
  }

  public void setAllowedValues(List<Expression> allowedValues) {
    this.allowedValues = allowedValues;
  }

  @Override
  @CheckForNull
  public Expression description() {
    return description;
  }

  public void setDescription(@Nullable Expression description) {
    this.description = description;
  }

  @Override
  @CheckForNull
  public Expression minValue() {
    return minValue;
  }

  public void setMinValue(@Nullable Expression minValue) {
    this.minValue = minValue;
  }

  @Override
  @CheckForNull
  public Expression maxValue() {
    return maxValue;
  }

  public void setMaxValue(@Nullable Expression maxValue) {
    this.maxValue = maxValue;
  }

  @Override
  @CheckForNull
  public Expression minLength() {
    return minLength;
  }

  public void setMinLength(@Nullable Expression minLength) {
    this.minLength = minLength;
  }

  @Override
  @CheckForNull
  public Expression maxLength() {
    return maxLength;
  }

  public void setMaxLength(@Nullable Expression maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  public Kind getKind() {
    return Kind.PARAMETER_DECLARATION;
  }
}
