/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.docker.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExpandableStringCharacters;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.RegularVariable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ExpandableStringLiteralImplTest {

  @Test
  void shouldParserExpandableStringLiteral() {
    Assertions.assertThat(DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL)
      .matches("\"\"")
      .matches("\"foo$bar\"")
      .matches("\"foo $bar\"")
      .matches("\"$bar\"")
      .matches("\"foo\"")
      .matches("\"foo$bar5a\"")
      .matches("\"foo$$bar\"")
      .matches("\"${foo}\"")
      .matches("\"foo ${bar}\"")
      .matches("\"1${bar}2\"")
      .matches("\"{}${bar}\"")

      .notMatches("'$foo'");
  }

  @Test
  void shouldSupportEmptyString() {
    ExpandableStringLiteral literal = parse("\"\"", DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL);
    assertThat(literal.getKind()).isEqualTo(DockerTree.Kind.EXPANDABLE_STRING_LITERAL);
    assertThat(literal.textRange()).hasRange(1, 0, 1, 2);
    assertThat(literal.expressions()).isEmpty();
  }

  @Test
  void shouldReturnElements() {
    ExpandableStringLiteral literal = parse("\"foo$bar\"", DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL);
    assertThat(literal.getKind()).isEqualTo(DockerTree.Kind.EXPANDABLE_STRING_LITERAL);
    assertThat(literal.textRange()).hasRange(1, 0, 1, 9);
    assertThat(literal.expressions()).hasSize(2);

    assertThat(literal.expressions().get(0)).isInstanceOfSatisfying(ExpandableStringCharacters.class, characters -> {
      assertThat(characters.getKind()).isEqualTo(DockerTree.Kind.EXPANDABLE_STRING_CHARACTERS);
      assertThat(characters.value()).isEqualTo("foo");
    });

    assertThat(literal.expressions().get(1)).isInstanceOfSatisfying(RegularVariable.class, variable -> {
      assertThat(variable.getKind()).isEqualTo(DockerTree.Kind.REGULAR_VARIABLE);
      assertThat(variable.identifier()).isEqualTo("bar");
    });
  }

  @Test
  void shouldConvertToString() {
    ExpandableStringLiteral literal = parse("\"foo$bar\"", DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL);
    assertThat(literal).hasToString("\"foo$bar\"");
  }

  @Test
  void shouldCheckEquality() {
    ExpandableStringLiteral literal1 = parse("\"foo$bar\"", DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL);
    ExpandableStringLiteral literal2 = parse("\"foo$bar\"", DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL);
    ExpandableStringLiteral literal3 = parse("\"bar$baz\"", DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL);

    assertThat(literal1)
      .isEqualTo(literal1)
      .isEqualTo(literal2)
      .hasSameHashCodeAs(literal2)
      .isNotEqualTo(literal3)
      .doesNotHaveSameHashCodeAs(literal3)
      .isNotEqualTo(null)
      .isNotEqualTo(new Object());
  }

  @Test
  void shouldCheckEqualityForStringCharacters() {
    ExpandableStringCharacters characters1 = parseCharacters("\"foo$bar\"");
    ExpandableStringCharacters characters2 = parseCharacters("\"foo$bar\"");
    ExpandableStringCharacters characters3 = parseCharacters("\"bar$baz\"");

    assertThat(characters1)
      .isEqualTo(characters1)
      .isEqualTo(characters2)
      .hasSameHashCodeAs(characters2)
      .isNotEqualTo(characters3)
      .doesNotHaveSameHashCodeAs(characters3)
      .isNotEqualTo(null)
      .isNotEqualTo(new Object());
  }

  private static ExpandableStringCharacters parseCharacters(String input) {
    return (ExpandableStringCharacters) ((ExpandableStringLiteral) parse(input, DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL)).expressions().get(0);
  }
}
