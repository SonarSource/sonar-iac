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
      .matches("@sys.decorator('parameter') import 'foo' with {} as bar")
      .matches("""
        @sys.decorator('parameter')
        @decorator()
        import 'foo' with {} as bar""")
      // defining an import as of name the same as keyword is possible
      .matches("import 'kubernetes@1.0.0' with {namespace: 'default'} as if")
      .matches("import 'kubernetes@1.0.0' with {namespace: 'default'} as type")
      .matches("import 'kubernetes@1.0.0' with {namespace: 'default'} as var")
      .matches("import 'kubernetes@1.0.0' with {namespace: 'default'} as param")
      .matches("import 'kubernetes@1.0.0' with {namespace: 'default'} as for")
      .matches("import 'kubernetes@1.0.0' with {namespace: 'default'} as func")

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
    ImportDeclaration tree = (ImportDeclaration) createParser(BicepLexicalGrammar.IMPORT_DECLARATION)
      .parse("@decorator('parameter') import 'kubernetes@1.0.0' with {} as k8s");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(ImportDeclaration.class);
    softly.assertThat(tree.decorators()).isNotNull().hasSize(1);
    softly.assertThat(tree.children()).hasSize(5);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.IMPORT_DECLARATION);
    softly.assertAll();
  }
}
