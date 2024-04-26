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
package org.sonar.iac.springconfig.tree.api;

import java.util.List;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.HasProperties;

/**
 * Represents a Spring configuration profile.
 * Each profile contains all key value pairs belonging to this specific spring profile.
 */
public interface Profile extends SpringConfig, HasProperties, HasComments {
  /**
   * @return a list of key value paris defined in this profile.
   */
  List<Tuple> properties();

  /**
   * @return a list of comments defined in this profile.
   */
  List<Comment> comments();

  /**
   * @return the name of this profile.
   */
  String name();

  /**
   * @return true if this profile is active.
   */
  boolean isActive();
}
