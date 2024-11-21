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
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.IndexSplatAccessTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class IndexSplatAccessTreeImpl extends TerraformTreeImpl implements IndexSplatAccessTree {
  private final ExpressionTree subject;
  private final SyntaxToken openBracket;
  private final SyntaxToken star;
  private final SyntaxToken closeBracket;

  public IndexSplatAccessTreeImpl(ExpressionTree subject, SyntaxToken openBracket, SyntaxToken star, SyntaxToken closeBracket) {
    this.subject = subject;
    this.openBracket = openBracket;
    this.star = star;
    this.closeBracket = closeBracket;
  }

  @Override
  public ExpressionTree subject() {
    return subject;
  }

  @Override
  public Kind getKind() {
    return Kind.INDEX_SPLAT_ACCESS;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(subject, openBracket, star, closeBracket);
  }
}
