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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.CompoundTypeReference;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class CompoundTypeReferenceImpl extends AbstractArmTreeImpl implements CompoundTypeReference {
  private final TypeExpressionAble baseType;
  private final Identifier suffix;

  public CompoundTypeReferenceImpl(TypeExpressionAble baseType, Identifier suffix) {
    this.baseType = baseType;
    this.suffix = suffix;
  }

  @Override
  public TypeExpressionAble baseType() {
    return baseType;
  }

  @Override
  public Identifier suffix() {
    return suffix;
  }

  @Override
  public List<Tree> children() {
    return List.of(baseType, suffix);
  }
}
