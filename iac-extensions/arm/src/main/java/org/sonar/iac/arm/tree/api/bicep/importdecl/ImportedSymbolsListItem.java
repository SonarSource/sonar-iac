package org.sonar.iac.arm.tree.api.bicep.importdecl;

import org.sonar.iac.arm.tree.api.ArmTree;

/**
 * An item in the list of imported symbols.
 */
public interface ImportedSymbolsListItem extends ArmTree {
  @Override
  default ArmTree.Kind getKind() {
    return Kind.IMPORTED_SYMBOLS_LIST_ITEM;
  }
}
