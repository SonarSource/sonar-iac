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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.MultilineString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class MultilineStringImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseMultilineString() {
    assertThat(BicepLexicalGrammar.MULTILINE_STRING)
      // .matches("''''''")
      .matches("'''python main.py'''")
      .matches("'''python main.py --abc ${{input.abc}} --def ${xyz}'''")
      .matches(code("'''",
        "first line",
        "second line",
        "'''"))
      .matches(code("'''",
        "first line",
        "// inline comment",
        "'''"))
      .matches(code("'''",
        "first line",
        "/* inline comment */",
        "'''"))
      .matches(code("'''",
        "first line",
        "/* inline",
        "comment */",
        "'''"))
      .matches(code("'''",
        "it's awesome",
        "'''"))
      .matches(code("'''",
        "it''s awesome",
        "'''"))

      .notMatches("''''")
      .notMatches("'''ab''''")
      .notMatches("''ab''''")
      .notMatches("''ab''")
      .notMatches("'ab'")
      .notMatches("'''ab'")
      .notMatches("'''ab''")
      .notMatches("\"\"\"ab\"\"\"")
      .notMatches("\"\"\"ab'''")
      .notMatches("'''a")
      .notMatches("''a")
      .notMatches("'a")
      .notMatches(code("'''",
        "ab",
        "''"))
      .notMatches(code("'''",
        "ab",
        "'"))
      .notMatches(code("'''",
        "ab"))
      .notMatches(code("'''",
        "ab",
        "''''"));
  }

  @Test
  void shouldParseSimpleMultilineString() {
    String code = code("'''",
      "a",
      "123",
      "BBB",
      "'''");
    MultilineString tree = parse(code, BicepLexicalGrammar.MULTILINE_STRING);
    Assertions.assertThat(tree.value()).isEqualTo("a\n123\nBBB\n");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.MULTILINE_STRING);
    Assertions.assertThat(tree.children())
      .map(c -> ((SyntaxToken) c).value())
      .containsExactly("'''", "a\n123\nBBB\n", "'''");
    assertThat(tree.textRange()).hasRange(1, 0, 5, 3);
  }
}
