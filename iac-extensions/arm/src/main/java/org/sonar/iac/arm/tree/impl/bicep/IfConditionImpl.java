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

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.bicep.IfCondition;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class IfConditionImpl extends AbstractArmTreeImpl implements IfCondition {

  private final SyntaxToken keyword;
  private final ParenthesizedExpression condition;
  private final ObjectExpression object;

  public IfConditionImpl(SyntaxToken keyword, ParenthesizedExpression condition, ObjectExpression object) {
    this.keyword = keyword;
    this.condition = condition;
    this.object = object;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, condition, object);
  }

  @Override
  public Kind getKind() {
    return Kind.IF_CONDITION;
  }

  @Override
  public Expression condition() {
    return condition.expression();
  }

  @Override
  public ObjectExpression object() {
    return object;
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }
}
