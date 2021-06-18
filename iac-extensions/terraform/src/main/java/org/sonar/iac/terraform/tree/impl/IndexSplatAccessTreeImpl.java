/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
