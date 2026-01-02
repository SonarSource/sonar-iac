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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.BooleanLiteral;

import static org.sonar.iac.arm.ArmAssertions.assertThat;

class BooleanLiteralImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseBooleanLiteral() {
    assertThat(BicepLexicalGrammar.BOOLEAN_LITERAL)
      .matches("true")
      .matches("false")

      .notMatches("TRUE")
      .notMatches("truer")
      .notMatches("FALSE")
      .notMatches("falser")
      .notMatches("True")
      .notMatches("False")
      .notMatches("1")
      .notMatches("0")
      .notMatches("");
  }

  @Test
  void shouldParseTrueValue() {
    BooleanLiteral tree = parse("true", BicepLexicalGrammar.BOOLEAN_LITERAL);
    assertThat(tree).isTrue();
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.BOOLEAN_LITERAL);
  }

  @Test
  void shouldParseFalseValue() {
    BooleanLiteral tree = parse("false", BicepLexicalGrammar.BOOLEAN_LITERAL);
    assertThat(tree).isFalse();
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.BOOLEAN_LITERAL);
  }
}
