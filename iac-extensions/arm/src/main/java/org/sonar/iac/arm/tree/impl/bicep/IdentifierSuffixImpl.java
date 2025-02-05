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

import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.IdentifierSuffix;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;

public class IdentifierSuffixImpl implements IdentifierSuffix {
  private final Identifier identifier;

  public IdentifierSuffixImpl(Identifier identifier) {
    this.identifier = identifier;
  }

  @Override
  public TypeExpressionAble applyTo(TypeExpressionAble baseType) {
    return new CompoundTypeReferenceImpl(baseType, identifier);
  }
}
