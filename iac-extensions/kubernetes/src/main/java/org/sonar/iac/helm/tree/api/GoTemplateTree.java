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

import java.util.List;
import org.sonar.iac.common.api.tree.Tree;

/**
 * An AST built from Go template.
 */
public interface GoTemplateTree extends Tree {
  /**
   * Name of the template.
   *
   * @return the name of the template
   */
  String name();

  /**
   * Name of the template as set during parsing.
   *
   * @return the parse name of the template
   */
  String parseName();

  /**
   * Parsing mode. See `text/template/parse` package for more details.
   *
   * @return the parsing mode
   */
  int mode();

  /**
   * Root of the AST.
   *
   * @return the root of the AST
   */
  ListNode root();

  @Override
  default List<Tree> children() {
    return List.of(root());
  }
}
