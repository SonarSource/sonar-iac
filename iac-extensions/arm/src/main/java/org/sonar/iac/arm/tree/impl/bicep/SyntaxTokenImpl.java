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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.Collections;
import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;

public class SyntaxTokenImpl extends AbstractArmTreeImpl implements SyntaxToken {

  private final String value;
  private final List<Comment> comments;

  public SyntaxTokenImpl(String value, TextRange textRange, List<Comment> comments) {
    this.value = value;
    this.textRange = textRange;
    this.comments = comments;
  }

  @Override
  public List<Comment> comments() {
    return comments;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }

  @Override
  public Kind getKind() {
    throw new UnsupportedOperationException("No kind for SyntaxToken");
  }

  @Override
  public String toString() {
    return value;
  }
}
