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
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ParameterDeclarationImpl extends AbstractArmTreeImpl implements ParameterDeclaration {

  private final Identifier identifier;
  private ParameterType type;
  private Expression defaultValue;
  private List<Expression> allowedValues;
  private Expression description;
  private Expression minValue;
  private Expression maxValue;
  private Expression minLength;
  private Expression maxLength;

  public ParameterDeclarationImpl(Identifier identifier) {
    this.identifier = identifier;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(identifier);

    return children;
  }

  @Override
  public Identifier identifier() {
    return identifier;
  }

  @Override
  public ParameterType type() {
    return type;
  }

  public void setType(ParameterType type) {
    this.type = type;
  }

  @Override
  public Expression defaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Expression defaultValue) {
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
  public Expression description() {
    return description;
  }

  public void setDescription(Expression description) {
    this.description = description;
  }

  @Override
  public Expression minValue() {
    return minValue;
  }

  public void setMinValue(Expression minValue) {
    this.minValue = minValue;
  }

  @Override
  public Expression maxValue() {
    return maxValue;
  }

  public void setMaxValue(Expression maxValue) {
    this.maxValue = maxValue;
  }

  @Override
  public Expression minLength() {
    return minLength;
  }

  public void setMinLength(Expression minLength) {
    this.minLength = minLength;
  }

  @Override
  public Expression maxLength() {
    return maxLength;
  }

  public void setMaxLength(Expression maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  public Kind getKind() {
    return Kind.PARAMETER_DECLARATION;
  }
}
