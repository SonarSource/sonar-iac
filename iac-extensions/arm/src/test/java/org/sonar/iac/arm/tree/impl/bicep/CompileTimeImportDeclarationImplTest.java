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
package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.CompileTimeImportDeclaration;

class CompileTimeImportDeclarationImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseValidStatement() {
    ArmAssertions.assertThat(BicepLexicalGrammar.COMPILE_TIME_IMPORT_DECLARATION)
      .matches("import {foo} from 'imports.bicep'")
      .matches("import { foo } from 'imports.bicep'")
      .matches("import {foo,bar} from 'imports.bicep'")
      .matches("import {foo, bar} from 'imports.bicep'")
      .matches("""
        import {
          foo as fizz
          bar as buzz
        } from 'imports.bicep'""")
      .matches("import * as baz from 'imports.bicep'")

      .notMatches("import *")
      .notMatches("import {foo, *} from 'imports.bicep'")
      .notMatches("import foo from 'imports.bicep'");
  }

  @Test
  void shouldParseWildcardImportStatement() {
    var tree = (CompileTimeImportDeclaration) createParser(BicepLexicalGrammar.COMPILE_TIME_IMPORT_DECLARATION)
      .parse("@decorator('parameter') import * as baz from 'imports.bicep'");

    var softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(CompileTimeImportDeclaration.class);
    softly.assertThat(tree.decorators()).isNotNull().hasSize(1);
    softly.assertThat(tree.children()).hasSize(4);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.COMPILE_TIME_IMPORT_DECLARATION);
    softly.assertThat(tree.target().getKind()).isEqualTo(ArmTree.Kind.COMPILE_TIME_IMPORT_TARGET);
    softly.assertThat(tree.target().children()).hasSize(2);
    softly.assertThat(tree.fromClause().getKind()).isEqualTo(ArmTree.Kind.COMPILE_TIME_IMPORT_FROM_CLAUSE);
    softly.assertThat(tree.fromClause().keyword().value()).isEqualTo("from");
    softly.assertAll();
  }

  @Test
  void shouldParseSymbolsListInImportStatement() {
    var tree = (CompileTimeImportDeclaration) createParser(BicepLexicalGrammar.COMPILE_TIME_IMPORT_DECLARATION)
      .parse("import {foo, bar} from 'imports.bicep'");

    var softly = new SoftAssertions();
    softly.assertThat(tree.decorators()).isEmpty();
    softly.assertThat(tree.children()).hasSize(3);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.COMPILE_TIME_IMPORT_DECLARATION);
    softly.assertThat(tree.target().getKind()).isEqualTo(ArmTree.Kind.COMPILE_TIME_IMPORT_TARGET);
    softly.assertThat(tree.target().children()).hasSize(5);
    softly.assertThat(((ArmTree) tree.target().children().get(1)).getKind()).isEqualTo(ArmTree.Kind.IMPORTED_SYMBOLS_LIST_ITEM);
    softly.assertAll();
  }
}
