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
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class IdentifierImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseIdentifier() {
    ArmAssertions.assertThat(BicepLexicalGrammar.IDENTIFIER)
      .matches("abc")
      .matches("A")
      .matches("Z")
      .matches("a")
      .matches("z")
      .matches("AAAAA123")
      .matches("aa222bbb")
      .matches("_A1")

      .notMatches("123zz")
      .notMatches("123aa789")
      .notMatches("123BB789")
      .notMatches("123")
      .notMatches(".123456")
      .notMatches("-")
      .notMatches("$123")
      .notMatches("{123}")
      .notMatches("(abc");
  }

  @Test
  void shouldParseSimpleIdentifier() {
    String code = "abc123DEF";

    Identifier tree = parse(code, BicepLexicalGrammar.IDENTIFIER);
    assertThat(tree.value()).isEqualTo("abc123DEF");
    assertThat(tree.is(ArmTree.Kind.IDENTIFIER)).isTrue();

    assertThat(tree.children()).hasSize(1);
    SyntaxToken token = (SyntaxToken) tree.children().get(0);
    assertThat(token.children()).isEmpty();
    assertThat(token.comments()).isEmpty();
  }

  @Test
  void shouldConvertToString() {
    String code = "abc123DEF";
    Identifier identifier = parse(code, BicepLexicalGrammar.IDENTIFIER);
    assertThat(identifier).hasToString("abc123DEF");
  }
}
