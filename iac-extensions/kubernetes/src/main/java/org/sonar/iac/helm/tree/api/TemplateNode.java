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
