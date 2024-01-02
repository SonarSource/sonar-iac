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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.impl.bicep.BicepTreeModelTest;

import static org.sonar.iac.arm.ArmAssertions.assertThat;

class ImportAsClauseImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseImportAsClause() {
    assertThat(BicepLexicalGrammar.IMPORT_AS_CLAUSE)
      .matches("as abc")
      .matches("as myName1")

      .notMatches("aas abc");
  }

  @Test
  void shouldParseSimpleImportAsClause() {
    ImportAsClauseImpl tree = parse("as abc", BicepLexicalGrammar.IMPORT_AS_CLAUSE);
    Assertions.assertThat(tree.keyword().value()).isEqualTo("as");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.IMPORT_AS_CLAUSE);
    Assertions.assertThat(tree.children()).hasSize(2);
    assertThat(tree.alias().getKind()).isEqualTo(ArmTree.Kind.IDENTIFIER);
    assertThat(tree.textRange()).hasRange(1, 0, 1, 6);
  }
}
