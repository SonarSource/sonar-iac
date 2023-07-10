package org.sonar.iac.arm.tree.impl.bicep.importdecl;

import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.Tree;

import java.util.List;

public class ImportAsClause {
  private final SyntaxToken keyword;
  private final Identifier alias;

  public ImportAsClause(SyntaxToken keyword, Identifier alias) {
    this.keyword = keyword;
    this.alias = alias;
  }

  public List<Tree> children() {
    return List.of(keyword, alias);
  }
}
