/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.AsClause;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportedSymbolsListItem;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public abstract class AbstractImportedSymbolsListItem<T extends Expression> extends AbstractArmTreeImpl implements ImportedSymbolsListItem {
  private final T identifier;
  private final AsClause asClause;

  protected AbstractImportedSymbolsListItem(T identifier, @Nullable AsClause asClause) {
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
