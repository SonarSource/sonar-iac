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
package org.sonar.iac.helm.tree.api;

import org.sonar.iac.common.api.tree.impl.TextRange;

/**
 * Location represents the location of a node in the input file.
 */
public interface Location {

  /**
   * @return the offset of the node in the input file
   */
  int position();

  /**
   * @return the length of the code fragment in the input file
   */
  int length();

  /**
   * Converts the location to {@link TextRange} for provided source code as parameter
   * @param sourceCode the text on witch conversion is calculated
   * @return the {@link TextRange} representation
   */
  TextRange toTextRange(String sourceCode);

}
