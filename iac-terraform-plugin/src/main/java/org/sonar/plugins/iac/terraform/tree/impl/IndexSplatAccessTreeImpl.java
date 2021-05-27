package org.sonar.plugins.iac.terraform.tree.impl;

import org.sonar.plugins.iac.terraform.api.tree.ExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.IndexSplatAccessTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;

import java.util.Arrays;
import java.util.List;

public class IndexSplatAccessTreeImpl extends TerraformTree implements IndexSplatAccessTree {
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
