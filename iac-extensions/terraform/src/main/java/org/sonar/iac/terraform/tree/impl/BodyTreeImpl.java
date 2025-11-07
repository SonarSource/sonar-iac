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
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.StatementTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class BodyTreeImpl extends TerraformTreeImpl implements BodyTree {

  private SyntaxToken openBrace;
  private SyntaxToken newlineToken;
  private List<StatementTree> statements;
  private SyntaxToken closeBrace;

  public BodyTreeImpl(SyntaxToken openBrace, @Nullable SyntaxToken newlineToken, List<StatementTree> statements, SyntaxToken closeBrace) {
    this.openBrace = openBrace;
    this.newlineToken = newlineToken;
    this.statements = statements;
    this.closeBrace = closeBrace;
  }

  @Override
  public List<StatementTree> statements() {
    return statements;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openBrace);
    if (newlineToken != null) {
      children.add(newlineToken);
    }
    children.addAll(statements);
    children.add(closeBrace);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.BODY;
  }
}
