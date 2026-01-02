/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.impl.json;

import java.util.Collections;
import java.util.List;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;

public class ObjectExpressionImpl extends AbstractArmTreeImpl implements ObjectExpression {

  private final List<Property> properties;

  public ObjectExpressionImpl(List<Property> properties, TextRange textRange) {
    this.properties = properties;
    this.textRange = textRange;
  }

  @Override
  public List<Property> properties() {
    return Collections.unmodifiableList(properties);
  }

  @Override
  public List<ResourceDeclaration> nestedResources() {
    return Collections.emptyList();
  }

  @Override
  public List<Tree> children() {
    return properties.stream().map(Tree.class::cast).toList();
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_EXPRESSION;
  }
}
