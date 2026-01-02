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

/**
 * Interface for type suffix that get applied to {@link TypeExpressionAble} types.
 */
public interface TypeReferenceSuffix {

  /**
   * Apply the suffix to another type.
   *
   * @param baseType the base type to which the suffix will be applied
   * @return the base type with the suffix
   */
  TypeExpressionAble applyTo(TypeExpressionAble baseType);
}
