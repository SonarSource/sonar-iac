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
package org.sonar.iac.arm.tree.impl.json;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.tree.ArmHelper.addChildrenIfPresent;

public class OutputDeclarationImpl extends AbstractArmTreeImpl implements OutputDeclaration {

  private final Identifier name;
  private final StringLiteral type;
  private final StringLiteral condition;
  private final StringLiteral copyCount;
  private final Expression copyInput;
  private final Expression value;

  public OutputDeclarationImpl(Identifier name, StringLiteral type, @Nullable StringLiteral condition, @Nullable StringLiteral copyCount,
    @Nullable Expression copyInput, @Nullable Expression value) {
    this.name = name;
    this.type = type;
    this.condition = condition;
    this.copyCount = copyCount;
    this.copyInput = copyInput;
    this.value = value;
  }

  @Override
  public Identifier declaratedName() {
    return name;
  }

  @Override
  public ArmTree type() {
    return type;
  }

  @CheckForNull
  @Override
  public StringLiteral condition() {
    return condition;
  }

  @CheckForNull
  @Override
  public StringLiteral copyCount() {
    return copyCount;
  }

  @CheckForNull
  @Override
  public Expression copyInput() {
    return copyInput;
  }

  @CheckForNull
  @Override
  public Expression value() {
    return value;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(name);
    children.add(type);
    addChildrenIfPresent(children, condition);
    addChildrenIfPresent(children, value);
    addChildrenIfPresent(children, copyCount);
    addChildrenIfPresent(children, copyInput);
    return children;
  }
}
