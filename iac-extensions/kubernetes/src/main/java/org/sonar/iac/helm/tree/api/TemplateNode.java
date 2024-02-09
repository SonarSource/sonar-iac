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

import javax.annotation.CheckForNull;

/**
 * TemplateNode represents a {{template}} action.
 */
public interface TemplateNode extends Node {
  @Override
  default NodeType type() {
    return NodeType.NODE_TEMPLATE;
  }

  /**
   * The name of the template (unquoted).
   *
   * @return the name of the template (unquoted)
   */
  @CheckForNull
  String name();

  /**
   * The command to evaluate as dot for the template.
   *
   * @return the command to evaluate as dot for the template
   */
  @CheckForNull
  PipeNode pipe();
}
