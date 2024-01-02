/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.bicep.ObjectProperty;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ObjectExpressionImpl extends AbstractArmTreeImpl implements ObjectExpression {

  private final SyntaxToken leftCurlyBrace;
  private final List<ObjectProperty> objectProperties;
  private final SyntaxToken rightCurlyBrace;

  public ObjectExpressionImpl(SyntaxToken leftCurlyBrace, List<ObjectProperty> objectProperties, SyntaxToken rightCurlyBrace) {
    this.leftCurlyBrace = leftCurlyBrace;
    this.objectProperties = objectProperties;
    this.rightCurlyBrace = rightCurlyBrace;
  }

  @Override
  public List<Property> properties() {
    return objectProperties.stream()
      .filter(Property.class::isInstance)
      .map(Property.class::cast)
      .collect(Collectors.toList());
  }

  @Override
  public List<ResourceDeclaration> nestedResources() {
    return objectProperties.stream()
      .filter(ResourceDeclaration.class::isInstance)
      .map(ResourceDeclaration.class::cast)
      .collect(Collectors.toList());
  }

  @Override
  public List<Tree> children() {
    List<Tree> list = new ArrayList<>();
    list.add(leftCurlyBrace);
    list.addAll(objectProperties);
    list.add(rightCurlyBrace);
    return list;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_EXPRESSION;
  }
}
