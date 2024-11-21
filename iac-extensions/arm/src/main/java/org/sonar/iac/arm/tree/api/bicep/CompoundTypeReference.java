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
