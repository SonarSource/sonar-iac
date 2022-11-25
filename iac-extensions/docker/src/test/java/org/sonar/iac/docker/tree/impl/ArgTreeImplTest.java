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
import org.sonar.iac.docker.tree.api.ArgTree;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.KeyValuePairTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ArgTreeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.ARG)
      .matches("ARG key1=value1")
      .matches("ARG key1")
      .matches("   ARG key1")
      .matches("arg key1")
      .matches("ARG key1=value1 key2=value2")
      .matches("ARG key1 key2")
      .matches("ARG key1 key2=value2")
      .matches("ARG key1= key2=value2")

      .notMatches("ARGkey1")
      .notMatches("ARG")
      .notMatches("ARG ")
    ;
  }

  @Test
  void argInstructionSimple() {
    ArgTree tree = parse("ARG key1", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 8);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.keyValuePairs()).hasSize(1);

    KeyValuePairTree keyValuePair = tree.keyValuePairs().get(0);
    assertThat(keyValuePair.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertThat(keyValuePair.key().value()).isEqualTo("key1");
    assertThat(keyValuePair.equals()).isNull();
    assertThat(keyValuePair.value()).isNull();
  }

  @Test
  void argInstructionWithDefaultValue() {
    ArgTree tree = parse("ARG key1=value1", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 15);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.keyValuePairs()).hasSize(1);

    KeyValuePairTree keyValuePair = tree.keyValuePairs().get(0);
    assertThat(keyValuePair.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertThat(keyValuePair.key().value()).isEqualTo("key1");
    assertThat(keyValuePair.equals().value()).isEqualTo("=");
    assertThat(keyValuePair.value().value()).isEqualTo("value1");
  }

  @Test
  void argInstructionMultipleValues() {
    ArgTree tree = parse("ARG key1=value1 key2", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 20);
    assertThat(tree.children()).hasSize(3);
    assertThat(tree.keyValuePairs()).hasSize(2);

    KeyValuePairTree keyValuePair1 = tree.keyValuePairs().get(0);
    assertThat(keyValuePair1.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertThat(keyValuePair1.key().value()).isEqualTo("key1");
    assertThat(keyValuePair1.equals().value()).isEqualTo("=");
    assertThat(keyValuePair1.value().value()).isEqualTo("value1");

    KeyValuePairTree keyValuePair2 = tree.keyValuePairs().get(1);
    assertThat(keyValuePair2.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertThat(keyValuePair2.key().value()).isEqualTo("key2");
    assertThat(keyValuePair2.equals()).isNull();
    assertThat(keyValuePair2.value()).isNull();
  }

  @Test
  void argInstructionMultipleValuesWithEquals() {
    ArgTree tree = parse("ARG key1=value1 key2=value2", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 27);
    assertThat(tree.children()).hasSize(3);
    assertThat(tree.keyValuePairs()).hasSize(2);

    KeyValuePairTree keyValuePair1 = tree.keyValuePairs().get(0);
    assertThat(keyValuePair1.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertThat(keyValuePair1.key().value()).isEqualTo("key1");
    assertThat(keyValuePair1.equals().value()).isEqualTo("=");
    assertThat(keyValuePair1.value().value()).isEqualTo("value1");

    KeyValuePairTree keyValuePair2 = tree.keyValuePairs().get(1);
    assertThat(keyValuePair2.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertThat(keyValuePair2.key().value()).isEqualTo("key2");
    assertThat(keyValuePair2.equals().value()).isEqualTo("=");
    assertThat(keyValuePair2.value().value()).isEqualTo("value2");
  }
}
