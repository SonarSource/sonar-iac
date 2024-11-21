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
package org.sonar.iac.jvmframeworkconfig.tree.api;

import java.util.List;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.HasProperties;

/**
 * Represents a Spring configuration profile.
 * Each profile contains all key value pairs belonging to this specific spring profile.
 */
public interface Profile extends JvmFrameworkConfig, HasProperties, HasComments {
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
