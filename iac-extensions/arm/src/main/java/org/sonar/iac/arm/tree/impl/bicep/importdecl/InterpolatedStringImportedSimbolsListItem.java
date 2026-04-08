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

import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.bicep.AsClause;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;

public class InterpolatedStringImportedSimbolsListItem extends AbstractImportedSymbolsListItem<InterpolatedString> {
  public InterpolatedStringImportedSimbolsListItem(InterpolatedString identifier, @Nullable AsClause asClause) {
    super(identifier, asClause);
  }
}
