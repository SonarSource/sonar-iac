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
package org.sonar.iac.arm.tree.api.bicep;

import org.sonar.iac.arm.tree.api.Identifier;

/**
 * A type reference that has a base type and a subtype separated with a dot.
 */
public interface CompoundTypeReference extends TypeExpressionAble {
  /**
   * The base type of the compound type reference.
   * @return the base type
   */
  TypeExpressionAble baseType();

  /**
   * The subtype of the compound type reference.
   * @return the subtype
   */
  Identifier suffix();

  @Override
  default Kind getKind() {
    return Kind.COMPOUND_TYPE_REFERENCE;
  }
}
