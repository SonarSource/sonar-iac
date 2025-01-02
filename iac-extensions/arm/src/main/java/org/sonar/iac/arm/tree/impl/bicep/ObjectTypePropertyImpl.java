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
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.ObjectTypeProperty;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

public class ObjectTypePropertyImpl extends AbstractArmTreeImpl implements ObjectTypeProperty {

  private final List<Decorator> decorators;
  private final TextTree name;
  private final SyntaxToken colon;
  private final TypeExpressionAble typeExpression;

  public ObjectTypePropertyImpl(List<Decorator> decorators, TextTree name, SyntaxToken colon, TypeExpressionAble typeExpression) {
    this.decorators = decorators;
    this.name = name;
    this.colon = colon;
    this.typeExpression = typeExpression;
  }

  @Override
  public TextTree name() {
    return name;
  }

  @Override
  public TypeExpressionAble typeExpression() {
    return typeExpression;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(decorators);
    children.add(name);
    children.add(colon);
    children.add(typeExpression);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_TYPE_PROPERTY;
  }

  @Override
  public List<Decorator> decorators() {
    return decorators;
  }
}
