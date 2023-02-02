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
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.LabelInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class LabelInstructionImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.LABEL)
      .matches("LABEL key1=value1")
      .matches("LABEL key1 value1")
      .matches("LABEL key1 value1 still_value1 again_value1")
      .matches("LABEL key1 \"value1\" still_value1 again_value1")
      .matches("    LABEL key1=value1")
      .matches("label key1=value1")
      .matches("LABEL key1=value1 key2=value2")
      .matches("LABEL \"key1\"=\"value1\" \"key2\"=\"value2\"")
      .matches("LABEL \"key1\"=value1 key2=\"value2\"")
      .notMatches("LABEL \"key1 value1 still_value1 again_value1\"")
      .notMatches("LABEL")
      .notMatches("LABEL key1")
      .notMatches("LABEL ");
  }

  @Test
  void labelInstructionWithoutEqualsOperator() {
    LabelInstruction tree = parse("LABEL key1 value1", DockerLexicalGrammar.LABEL);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.LABEL);
    assertThat(tree.keyword().value()).isEqualTo("LABEL");
    assertThat(tree.textRange().start().line()).isEqualTo(1);
    assertThat(tree.textRange().start().lineOffset()).isZero();
    assertThat(tree.textRange().end().line()).isEqualTo(1);
    assertThat(tree.textRange().end().lineOffset()).isEqualTo(17);
    assertThat(tree.children()).hasSize(3);
    assertThat(tree.keyValuePairs()).hasSize(1);

    KeyValuePair keyValuePair = tree.keyValuePairs().get(0);
    assertThat(keyValuePair.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertThat(keyValuePair.key().value()).isEqualTo("key1");
    assertThat(keyValuePair.equals()).isNull();
    assertThat(keyValuePair.value().value()).isEqualTo("value1");
  }

  @Test
  void labelInstructionWithoutEqualsOperatorLong() {
    LabelInstruction tree = parse("LABEL key1 value1 still_value1 again_value1", DockerLexicalGrammar.LABEL);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.LABEL);
    assertThat(tree.keyword().value()).isEqualTo("LABEL");
    assertThat(tree.keyValuePairs()).hasSize(1);
    KeyValuePair keyValuePair = tree.keyValuePairs().get(0);
    assertThat(keyValuePair.key().value()).isEqualTo("key1");
    assertThat(keyValuePair.value().value()).isEqualTo("value1 still_value1 again_value1");
    assertThat(tree.children()).hasSize(3);
  }

  @Test
  void labelInstructionWithEqualsOperator() {
    LabelInstruction tree = parse("LABEL key1=value1", DockerLexicalGrammar.LABEL);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.LABEL);
    assertThat(tree.keyword().value()).isEqualTo("LABEL");
    assertThat(tree.keyValuePairs()).hasSize(1);
    KeyValuePair keyValuePair = tree.keyValuePairs().get(0);
    assertThat(keyValuePair.key().value()).isEqualTo("key1");
    assertThat(keyValuePair.equals().value()).isEqualTo("=");
    assertThat(keyValuePair.value().value()).isEqualTo("value1");
    assertThat(tree.textRange().start().line()).isEqualTo(1);
    assertThat(tree.textRange().start().lineOffset()).isZero();
    assertThat(tree.textRange().end().line()).isEqualTo(1);
    assertThat(tree.textRange().end().lineOffset()).isEqualTo(17);
    assertThat(tree.children()).hasSize(4);
  }

  @Test
  void labelInstructionWithEqualsOperatorMultipleValues() {
    LabelInstruction tree = parse("LABEL key1=value1 key2=value2", DockerLexicalGrammar.LABEL);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.LABEL);
    assertThat(tree.keyword().value()).isEqualTo("LABEL");
    assertThat(tree.keyValuePairs()).hasSize(2);
    assertThat(tree.textRange().start().line()).isEqualTo(1);
    assertThat(tree.textRange().start().lineOffset()).isZero();
    assertThat(tree.textRange().end().line()).isEqualTo(1);
    assertThat(tree.textRange().end().lineOffset()).isEqualTo(29);
    assertThat(tree.children()).hasSize(7);

    KeyValuePair keyValuePair1 = tree.keyValuePairs().get(0);
    assertThat(keyValuePair1.key().value()).isEqualTo("key1");
    assertThat(keyValuePair1.equals().value()).isEqualTo("=");
    assertThat(keyValuePair1.value().value()).isEqualTo("value1");
    KeyValuePair keyValuePair2 = tree.keyValuePairs().get(1);
    assertThat(keyValuePair2.key().value()).isEqualTo("key2");
    assertThat(keyValuePair2.equals().value()).isEqualTo("=");
    assertThat(keyValuePair2.value().value()).isEqualTo("value2");
  }

  @Test
  void labelInstructionWithEqualsOperatorMultipleValuesQuotes() {
    LabelInstruction tree = parse("LABEL \"key1\"=\"value1\" \"key2\"=value2 key3=\"value3\"", DockerLexicalGrammar.LABEL);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.LABEL);
    assertThat(tree.keyword().value()).isEqualTo("LABEL");
    assertThat(tree.keyValuePairs()).hasSize(3);
    assertThat(tree.textRange().start().line()).isEqualTo(1);
    assertThat(tree.textRange().start().lineOffset()).isZero();
    assertThat(tree.textRange().end().line()).isEqualTo(1);
    assertThat(tree.textRange().end().lineOffset()).isEqualTo(49);
    assertThat(tree.children()).hasSize(10);

    KeyValuePair keyValuePair1 = tree.keyValuePairs().get(0);
    assertThat(keyValuePair1.key().value()).isEqualTo("\"key1\"");
    assertThat(keyValuePair1.equals().value()).isEqualTo("=");
    assertThat(keyValuePair1.value().value()).isEqualTo("\"value1\"");
    KeyValuePair keyValuePair2 = tree.keyValuePairs().get(1);
    assertThat(keyValuePair2.key().value()).isEqualTo("\"key2\"");
    assertThat(keyValuePair2.equals().value()).isEqualTo("=");
    assertThat(keyValuePair2.value().value()).isEqualTo("value2");
    KeyValuePair keyValuePair3 = tree.keyValuePairs().get(2);
    assertThat(keyValuePair3.key().value()).isEqualTo("key3");
    assertThat(keyValuePair3.equals().value()).isEqualTo("=");
    assertThat(keyValuePair3.value().value()).isEqualTo("\"value3\"");
  }
}
