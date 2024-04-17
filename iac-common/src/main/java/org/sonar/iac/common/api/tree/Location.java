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
package org.sonar.iac.common.api.tree;

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

  /**
   * Returns new shifted Location
   * @param positionShift how much shift position
   * @param lengthShift how much shift length
   * @return new shifted Location
   */
  Location shift(int positionShift, int lengthShift);
}
