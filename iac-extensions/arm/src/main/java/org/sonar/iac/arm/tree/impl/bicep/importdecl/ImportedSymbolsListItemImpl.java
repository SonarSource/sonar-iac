package org.sonar.iac.arm.tree.impl.bicep.importdecl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportAsClause;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportedSymbolsListItem;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ImportedSymbolsListItemImpl extends AbstractArmTreeImpl implements ImportedSymbolsListItem {
  private final Identifier identifier;
  private final ImportAsClause importAsClause;

  public ImportedSymbolsListItemImpl(Identifier identifier, @Nullable ImportAsClause importAsClause) {
    this.identifier = identifier;
    this.importAsClause = importAsClause;
  }

  @Override
  public List<Tree> children() {
    var result = new ArrayList<Tree>();
    result.add(identifier);
    if (importAsClause != null) {
      result.add(importAsClause);
    }
    return result;
  }
}
