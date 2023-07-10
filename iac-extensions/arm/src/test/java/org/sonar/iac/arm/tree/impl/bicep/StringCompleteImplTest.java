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
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.parser.utils.Assertions;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class StringCompleteImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.STRING_COMPLETE);

  @Test
  void shouldParseSimpleStringComplete() {
    String code = code("'abc123DEF'");

    StringComplete tree = (StringComplete) parser.parse(code, null);
    assertThat(tree.value()).isEqualTo("abc123DEF");
    assertThat(tree.is(ArmTree.Kind.STRING_COMPLETE)).isTrue();

    SyntaxToken token1 = (SyntaxToken) tree.children().get(0);
    assertThat(token1.value()).isEqualTo("'");

    SyntaxToken token2 = (SyntaxToken) tree.children().get(1);
    assertThat(token2.children()).isEmpty();
    assertThat(token2.comments()).isEmpty();

    SyntaxToken token3 = (SyntaxToken) tree.children().get(2);
    assertThat(token3.value()).isEqualTo("'");

    assertThat(tree.children()).hasSize(3);
  }

  @Test
  void shouldParseStringComplete() {
    Assertions.assertThat(BicepLexicalGrammar.STRING_COMPLETE)
      .matches("'123'")
      .matches("'abc'")
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

      .notMatches(".12'3456")
      .notMatches("-")
      .notMatches("_A1")
      .notMatches("$123'")
      .notMatches("{123}")
      .notMatches("(abc");
  }
}
