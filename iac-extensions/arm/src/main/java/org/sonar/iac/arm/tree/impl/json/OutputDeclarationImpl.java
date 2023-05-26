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
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class OutputDeclarationImpl extends AbstractArmTreeImpl implements OutputDeclaration {

  private final Identifier name;
  private final Property type;
  private final Property condition;
  private final Property copyCount;
  private final Property copyInput;
  private final Property value;

  public OutputDeclarationImpl(Identifier name, Property type, @Nullable Property condition, @Nullable Property copyCount, @Nullable Property copyInput, @Nullable Property value) {
    this.name = name;
    this.type = type;
    this.condition = condition;
    this.copyCount = copyCount;
    this.copyInput = copyInput;
    this.value = value;
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public String type() {
    return type.value().value();
  }

  @CheckForNull
  @Override
  public String condition() {
    return propertyValueToString(condition);
  }

  @CheckForNull
  @Override
  public String copyCount() {
    return propertyValueToString(copyCount);
  }

  @CheckForNull
  @Override
  public String copyInput() {
    return propertyValueToString(copyInput);
  }

  @CheckForNull
  @Override
  public String value() {
    return propertyValueToString(value);
  }

  @CheckForNull
  private static String propertyValueToString(@Nullable Property property) {
    return Optional.ofNullable(property)
      .map(Property::value)
      .map(Expression::value)
      .orElse(null);
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(name);
    children.add(type.key());
    children.add(type.value());
    addChildrenIfPresent(children, condition);
    addChildrenIfPresent(children, value);
    addChildrenIfPresent(children, copyCount);
    addChildrenIfPresent(children, copyInput);
    return children;
  }

  private static void addChildrenIfPresent(List<Tree> children, @Nullable Property property) {
    if (property != null) {
      children.add(property.key());
      children.add(property.value());
    }
  }

  @Override
  public Kind getKind() {
    return Kind.OUTPUT_DECLARATION;
  }
}
