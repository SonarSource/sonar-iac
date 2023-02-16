/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.tree.impl;


import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.KeyValuePair;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class KeyValuePairImplTest {

  @Test
  void matchingKeyValuePair() {
    Assertions.assertThat(DockerLexicalGrammar.KEY_VALUE_PAIR)
      // Pairs with equal sign
      .matches("\"key\"=\"value\"")
      .matches("key=value")
      .matches("$key=\"value\"")
      .matches("$key=$value")
      .matches("foo$bar=value")
      .matches("key=value=")
      .matches("${key}=value=")
      .matches("key=")
      .matches("ke\\\"y=value")
      .matches("key=val\\\"ue")

      // Pairs without equal sign and without value
      .matches("\"key\"")
      .matches("key")
      .matches("$key")
      .matches("prefix_$key")

      // Pairs without equal sign
      .matches("\"key\" \"value\"")
      .matches("\"key\" \"value1\" \"value2\"")
      .matches("key $value1 = $value2")
      .matches("key $value1 = ${value2}")

      .matches("\"key=\"=value") // This is invalid syntax, but it would increase the grammar complexity to cover this case

      .notMatches("key= value")
      .notMatches("\"key\"= \"value\"")
    ;
  }

  @Test
  void shouldProvideAllRelevantInfoForKvpWithEqualSign() {
    KeyValuePair keyValuePair = parse("key=value", DockerLexicalGrammar.KEY_VALUE_PAIR);

    assertThat(keyValuePair.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertTextRange(keyValuePair.textRange()).hasRange(1, 0, 1, 9);

    assertTextRange(keyValuePair.key().textRange()).hasRange(1, 0 , 1, 3);

    assertThat(keyValuePair.equalSign()).satisfies(equal -> {
      assertThat(equal).isNotNull();
      assertTextRange(equal.textRange()).hasRange(1, 3 , 1, 4);
    });assertThat(keyValuePair.equalSign()).satisfies(equal -> {
      assertThat(equal).isNotNull();
      assertTextRange(equal.textRange()).hasRange(1, 3 , 1, 4);
    });

    assertThat(keyValuePair.value()).satisfies(key -> {
      assertThat(key).isNotNull();
      assertTextRange(key.textRange()).hasRange(1, 4 , 1, 9);
    });
  }

  @Test
  void shouldProvideAllRelevantInfoForKvpAsSingleKey() {
    KeyValuePair keyValuePair = parse("key", DockerLexicalGrammar.KEY_VALUE_PAIR);

    assertThat(keyValuePair.equalSign()).isNull();
    assertThat(keyValuePair.value()).isNull();
  }

  @Test
  void shouldProvideAllRelevantInfoForKvpWithoutEqualSign() {
    KeyValuePair keyValuePair = parse("key value1    value2", DockerLexicalGrammar.KEY_VALUE_PAIR);

    assertThat(keyValuePair.equalSign()).isNull();
    assertThat(keyValuePair.value()).isNotNull().satisfies(argument ->
      assertThat(argument.expressions())
        .hasSize(3)
        .hasExactlyElementsOfTypes(LiteralImpl.class, LiteralImpl.class, LiteralImpl.class)
        .extracting(expression -> ((Literal) expression).value())
        .containsExactly("value1", "    ", "value2"));
  }
}
