package org.sonar.iac.arm.tree.impl.bicep.importdecl;

import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.Tree;

import java.util.List;

public class ImportWithClause {
  private final SyntaxToken keyword;
  private final ObjectExpression object;

  public ImportWithClause(SyntaxToken keyword, ObjectExpression object) {
    this.keyword = keyword;
    this.object = object;
  }

  public List<Tree> children() {
    return List.of(keyword, object);
  }
}
