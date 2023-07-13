/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.sonar.iac.arm.tree.api.bicep.ImportDeclaration;

class ImportDeclarationImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseValidStatement() {
    ArmAssertions.assertThat(BicepLexicalGrammar.IMPORT_DECLARATION)
      .matches("import 'foo'")
      .matches("import 'foo' as bar")
      .matches("import 'foo' with {}")
      .matches("import 'foo' with {} as bar")
      .matches("@decorator('parameter') import 'foo' with {} as bar")

      .notMatches("import")
      .notMatches("import with {}")
      .notMatches("import as bar")
      .notMatches("@decorator('parameter') import")
      .notMatches("import 'foo' as bar with")
      .notMatches("import 'foo' with as bar")
      .notMatches("import 'foo' as");
  }

  @Test
  void shouldParseImportStatement() {
    ArmTree tree = createParser(BicepLexicalGrammar.IMPORT_DECLARATION)
      .parse("@decorator('parameter') import 'kubernetes@1.0.0' with {} as k8s");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(ImportDeclaration.class);
    softly.assertThat(tree.children()).hasSize(7);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.IMPORT_DECLARATION);
    softly.assertAll();
  }
}
