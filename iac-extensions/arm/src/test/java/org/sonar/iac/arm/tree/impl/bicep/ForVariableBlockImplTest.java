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
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.ForVariableBlock;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ForVariableBlockImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.FOR_VARIABLE_BLOCK);

  @Test
  void shouldParseForVariableBlock() {
    ArmAssertions.assertThat(BicepLexicalGrammar.FOR_VARIABLE_BLOCK)
      .matches("identifier123")
      .matches("(itemIdentifier123,indexIdentifier123)")
      .matches("(itemIdentifier123 , indexIdentifier123)")
      .matches("(itemIdentifier123, indexIdentifier123)")
      .matches("(itemIdentifier123 ,indexIdentifier123)")

      .notMatches("()")
      .notMatches("(,)")
      .notMatches("(indexIdentifier123,)")
      .notMatches("(,itemIdentifier123)")
      .notMatches("(indexIdentifier123,itemIdentifier123,)");
  }

  @Test
  void shouldParseForVariableBlockWithDetailedAssertions() {
    String code = code("(itemIdentifier123,indexIdentifier123)");

    ForVariableBlock tree = (ForVariableBlock) parser.parse(code, null);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.FOR_VARIABLE_BLOCK)).isTrue();

    softly.assertThat(tree.itemIdentifier().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    softly.assertThat(tree.itemIdentifier().value()).isEqualTo("itemIdentifier123");

    softly.assertThat(tree.indexIdentifier().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    softly.assertThat(tree.indexIdentifier().value()).isEqualTo("indexIdentifier123");

    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("(", "itemIdentifier123", ",", "indexIdentifier123", ")");

    softly.assertAll();
  }

}
