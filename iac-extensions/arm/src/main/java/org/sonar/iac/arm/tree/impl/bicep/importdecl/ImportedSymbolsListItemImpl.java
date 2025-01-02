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
package org.sonar.iac.arm.tree.impl.bicep.importdecl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.AsClause;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportedSymbolsListItem;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ImportedSymbolsListItemImpl extends AbstractArmTreeImpl implements ImportedSymbolsListItem {
  private final Identifier identifier;
  private final AsClause asClause;

  public ImportedSymbolsListItemImpl(Identifier identifier, @Nullable AsClause asClause) {
    this.identifier = identifier;
    this.asClause = asClause;
  }

  @Override
  public List<Tree> children() {
    var result = new ArrayList<Tree>();
    result.add(identifier);
    if (asClause != null) {
      result.add(asClause);
    }
    return result;
  }
}
