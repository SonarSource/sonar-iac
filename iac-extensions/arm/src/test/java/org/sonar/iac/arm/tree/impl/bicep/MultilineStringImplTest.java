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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.MultilineString;

import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class MultilineStringImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseMultilineString() {
    assertThat(BicepLexicalGrammar.MULTILINE_STRING)
      .matches("''''''")
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
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("'''", "a\n123\nBBB\n", "'''");
    assertThat(tree.textRange()).hasRange(1, 0, 5, 3);
  }
}
