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
package org.sonar.iac.helm.tree.api;

/**
 * Location represents the location of a node in the input file.
 */
public class Location {
  /**
   * The offset of the node in the input file.
   */
  private final long position;

  /**
   * The length of the piece of code in the input file that the node represents.
   */
  private final long length;

  /**
   * Constructs a location with the given position and length.
   *
   * @param position the offset of the node in the input file
   * @param length the length of the code fragment in the input file
   */
  public Location(long position, long length) {
    this.position = position;
    this.length = length;
  }

  /**
   * @return the offset of the node in the input file
   */
  public long position() {
    return position;
  }

  /**
   * @return the length of the code fragment in the input file
   */
  public long length() {
    return length;
  }
}
