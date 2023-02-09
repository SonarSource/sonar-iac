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
import org.sonar.iac.docker.tree.api.NewKeyValuePair;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class NewKeyValuePairImplTest {

  @Test
  void matchingKeyValuePair() {
    Assertions.assertThat(DockerLexicalGrammar.KEY_VALUE_PAIR)
      .matches("\"key\"=\"value\"")
      .matches("key=value")
      .matches("$key=\"value\"")
      .matches("$key=$value")
      .matches("foo$bar=value")
      .matches("key=value=")
      .matches("${key}=value=")
      .matches("key=")

      .matches("\"key=\"=value") // This is invalid syntax, but it would increase the grammar complexity to cover this case

      .notMatches("key")
      .notMatches("key= value")
      .notMatches("\"key\"= \"value\"")
    ;
  }

  @Test
  void shouldProvideAllRelevantInfo() {
    NewKeyValuePair keyValuePair = parse("key=value", DockerLexicalGrammar.KEY_VALUE_PAIR);

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
}
