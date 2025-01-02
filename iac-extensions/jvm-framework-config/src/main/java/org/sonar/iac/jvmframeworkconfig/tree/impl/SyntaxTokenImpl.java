/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.jvmframeworkconfig.tree.impl;

import java.util.Collections;
import java.util.List;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.jvmframeworkconfig.tree.api.SyntaxToken;

public class SyntaxTokenImpl extends AbstractJvmFrameworkConfigImpl implements SyntaxToken {
  private final String value;

  public SyntaxTokenImpl(String value, TextRange textRange) {
    this.value = value;
    this.textRange = textRange;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public List<Comment> comments() {
    return List.of();
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }
}
