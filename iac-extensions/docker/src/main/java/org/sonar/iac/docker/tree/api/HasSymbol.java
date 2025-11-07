/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.docker.tree.api;

import javax.annotation.Nullable;
import org.sonar.iac.docker.symbols.Symbol;

/**
 * Interface to define an element which has a {@link Symbol}, with methods to modify/access it.
 */
public interface HasSymbol {
  /**
   * @return the {@link Symbol} associated with this element, or null if not set.
   */
  @Nullable
  Symbol symbol();

  /**
   * @throws IllegalArgumentException when symbol is already set.
   */
  void setSymbol(Symbol symbol);
}
