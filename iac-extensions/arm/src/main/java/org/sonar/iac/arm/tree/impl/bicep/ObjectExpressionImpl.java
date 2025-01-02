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
import java.util.stream.Collectors;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.bicep.ObjectProperty;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public class ObjectExpressionImpl extends AbstractArmTreeImpl implements ObjectExpression {

  private final SyntaxToken leftCurlyBrace;
  private final SeparatedList<ObjectProperty, SyntaxToken> objectPropertiesWithSeparators;
  private final SyntaxToken rightCurlyBrace;

  public ObjectExpressionImpl(SyntaxToken leftCurlyBrace, SeparatedList<ObjectProperty, SyntaxToken> objectPropertiesWithSeparators, SyntaxToken rightCurlyBrace) {
    this.leftCurlyBrace = leftCurlyBrace;
    this.objectPropertiesWithSeparators = objectPropertiesWithSeparators;
    this.rightCurlyBrace = rightCurlyBrace;
  }

  @Override
  public List<Property> properties() {
    return objectPropertiesWithSeparators.elements().stream()
      .filter(Property.class::isInstance)
      .map(Property.class::cast)
      .toList();
  }

  @Override
  public List<ResourceDeclaration> nestedResources() {
    return objectPropertiesWithSeparators.elements().stream()
      .filter(ResourceDeclaration.class::isInstance)
      .map(ResourceDeclaration.class::cast)
      .toList();
  }

  @Override
  public List<Tree> children() {
    List<Tree> list = new ArrayList<>();
    list.add(leftCurlyBrace);
    list.addAll(objectPropertiesWithSeparators.elementsAndSeparators());
    list.add(rightCurlyBrace);
    return list;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_EXPRESSION;
  }

  @Override
  public String toString() {
    String propertiesString = objectPropertiesWithSeparators.elements().stream()
      .map(ObjectProperty::toString)
      .collect(Collectors.joining(", "));
    return "{" + propertiesString + "}";
  }
}
