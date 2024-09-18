/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
