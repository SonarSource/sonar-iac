/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.StringLiteral;

import static org.assertj.core.api.Assertions.assertThat;

class StringLiteralImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseStringLiteral() {
    ArmAssertions.assertThat(BicepLexicalGrammar.STRING_LITERAL)
      .matches("'123'")
      .matches("'abc'")
      .matches("'ab\\'c'")
      .matches("  'abc'")
      .matches("'A'")
      .matches("'Z'")
      .matches("'a'")
      .matches("'z'")
      .matches("'AAAAA123'")
      .matches("'123zz'")
      .matches("'123aa789'")
      .matches("'123BB789'")
      .matches("'a$b'")
      .matches("'a{}b'")
      .matches("'a//a'")
      .matches("'a#a'")
      .matches("'#a'")
      .matches("'//a'")

      .notMatches(".12'3456")
      .notMatches("-")
      .notMatches("_A1")
      .notMatches("\\'abc'")
      .notMatches("'ab\\\\'c'")
      .notMatches("'abc\\'")
      .notMatches("$123'")
      .notMatches("{123}")
      .notMatches("(abc");
  }

  @Test
  void shouldParseSimpleStringLiteral() {
    String code = "'abc123DEF'";

    StringLiteral tree = parse(code, BicepLexicalGrammar.STRING_LITERAL);
    assertThat(tree.value()).isEqualTo("abc123DEF");
    assertThat(tree.is(ArmTree.Kind.STRING_LITERAL)).isTrue();
  }

  @Test
  void shouldConvertToString() {
    String code = "'abc123DEF'";
    StringLiteral stringLiteral = parse(code, BicepLexicalGrammar.STRING_LITERAL);
    assertThat(stringLiteral).hasToString("'abc123DEF'");
  }
}
