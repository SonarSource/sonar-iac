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
  private final Property defaultValue;
  private final List<Expression> allowedValues;
  private final Property description;
  private final Property minValue;
  private final Property maxValue;
  private final Property minLength;
  private final Property maxLength;

  // Methods should not have too many parameters
  @SuppressWarnings("java:S107")
  public ParameterDeclarationImpl(
    Identifier identifier,
    Property type,
    @Nullable Property defaultValue,
    List<Expression> allowedValues,
    @Nullable Property description,
    @Nullable Property minValue,
    @Nullable Property maxValue,
    @Nullable Property minLength,
    @Nullable Property maxLength) {

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
    addChildrenIfPresent(children, defaultValue);
    addChildrenIfPresent(children, description);
    addChildrenIfPresent(children, minValue);
    addChildrenIfPresent(children, maxValue);
    addChildrenIfPresent(children, minLength);
    addChildrenIfPresent(children, maxLength);
    children.addAll(allowedValues);
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
    if (defaultValue != null) {
      return defaultValue.value();
    }
    return null;
  }

  @Override
  public List<Expression> allowedValues() {
    return allowedValues;
  }

  @Override
  @CheckForNull
  public Expression description() {
    if (description != null) {
      return description.value();
    }
    return null;
  }

  @Override
  @CheckForNull
  public Expression minValue() {
    if (minValue != null) {
      return minValue.value();
    }
    return null;
  }

  @Override
  @CheckForNull
  public Expression maxValue() {
    if (maxValue != null) {
      return maxValue.value();
    }
    return null;
  }

  @Override
  @CheckForNull
  public Expression minLength() {
    if (minLength != null) {
      return minLength.value();
    }
    return null;
  }

  @Override
  @CheckForNull
  public Expression maxLength() {
    if (maxLength != null) {
      return maxLength.value();
    }
    return null;
  }

  @Override
  public Kind getKind() {
    return Kind.PARAMETER_DECLARATION;
  }

  private static void addChildrenIfPresent(List<Tree> children, @Nullable Property property) {
    if (property != null) {
      children.add(property.key());
      children.add(property.value());
    }
  }
}
