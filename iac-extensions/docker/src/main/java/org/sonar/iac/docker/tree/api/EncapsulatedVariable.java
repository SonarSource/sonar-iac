/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.docker.tree.api;

import javax.annotation.Nullable;

/**
 * Represent a variable in the explicit form with the curly braces which allow to specify modifiers.
 * This interface define methods to retrieve both the modifier separator and the modifier itself, as it support modifier for docker variable
 * but also more generally for shell variables.
 * Examples :
 * {@code ${var}}
 * {@code ${var:-modifier}}
 */
public interface EncapsulatedVariable extends Variable {

  @Nullable
  String modifierSeparator();

  @Nullable
  Argument modifier();
}
