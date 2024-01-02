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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

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
    String code = code("abc123DEF");

    Identifier tree = parse(code, BicepLexicalGrammar.IDENTIFIER);
    assertThat(tree.value()).isEqualTo("abc123DEF");
    assertThat(tree.is(ArmTree.Kind.IDENTIFIER)).isTrue();

    assertThat(tree.children()).hasSize(1);
    SyntaxToken token = (SyntaxToken) tree.children().get(0);
    assertThat(token.children()).isEmpty();
    assertThat(token.comments()).isEmpty();
  }
}
