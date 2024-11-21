/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
