/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.arm.tree.impl.bicep.importdecl;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.importdecl.CompileTimeImportTarget;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportedSymbolsListItem;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public class ImportedSymbolsList extends AbstractArmTreeImpl implements CompileTimeImportTarget {
  private final SyntaxToken openCurly;
  private final SeparatedList<ImportedSymbolsListItem, SyntaxToken> importedSymbols;
  private final SyntaxToken closeCurly;

  public ImportedSymbolsList(SyntaxToken openCurly, SeparatedList<ImportedSymbolsListItem, SyntaxToken> importedSymbols, SyntaxToken closeCurly) {
    this.openCurly = openCurly;
    this.importedSymbols = importedSymbols;
    this.closeCurly = closeCurly;
  }

  @Override
  public List<Tree> children() {
    var result = new ArrayList<Tree>();
    result.add(openCurly);
    result.addAll(importedSymbols.elementsAndSeparators());
    result.add(closeCurly);
    return result;
  }
}
