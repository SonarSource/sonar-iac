/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.impl.bicep.BicepTreeModelTest;

import static org.sonar.iac.arm.ArmAssertions.assertThat;

class ImportWithClauseImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseImportWthClause() {
    assertThat(BicepLexicalGrammar.IMPORT_WITH_CLAUSE)
      .matches("with {}")

      .notMatches("with")
      .notMatches("as abc");
  }

  @Test
  void shouldParseSimpleImportWithClause() {
    ImportWithClauseImpl tree = parse("with {}", BicepLexicalGrammar.IMPORT_WITH_CLAUSE);
    Assertions.assertThat(tree.keyword().value()).isEqualTo("with");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.IMPORT_WITH_CLAUSE);
    Assertions.assertThat(tree.children()).hasSize(2);
    assertThat(tree.object().getKind()).isEqualTo(ArmTree.Kind.OBJECT_EXPRESSION);
    assertThat(tree.textRange()).hasRange(1, 0, 1, 7);
  }
}
