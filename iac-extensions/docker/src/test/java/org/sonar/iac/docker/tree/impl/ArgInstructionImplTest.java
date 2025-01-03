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
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.KeyValuePair;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.DockerAssertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ArgInstructionImplTest {
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
      .notMatches("ARG ");
  }

  @Test
  void argInstructionSimple() {
    ArgInstruction tree = parse("ARG key1", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 8);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.keyValuePairs()).hasSize(1);

    KeyValuePair keyValuePair = tree.keyValuePairs().get(0);
    assertThat(keyValuePair.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertThat(keyValuePair)
      .hasKey("key1");
  }

  @Test
  void argInstructionWithDefaultValue() {
    ArgInstruction tree = parse("ARG key1=value1", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 15);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.keyValuePairs()).hasSize(1);

    KeyValuePair keyValuePair = tree.keyValuePairs().get(0);
    assertThat(keyValuePair)
      .hasKey("key1")
      .hasValue("value1");
  }

  @Test
  void argInstructionMultipleValues() {
    ArgInstruction tree = parse("ARG key1=value1 key2", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 20);
    assertThat(tree.children()).hasSize(3);
    assertThat(tree.keyValuePairs()).hasSize(2);

    KeyValuePair keyValuePair1 = tree.keyValuePairs().get(0);
    assertThat(keyValuePair1)
      .hasKey("key1")
      .hasValue("value1");

    KeyValuePair keyValuePair2 = tree.keyValuePairs().get(1);
    assertThat(keyValuePair2)
      .hasKey("key2")
      .hasValueNull();
  }

  @Test
  void argInstructionMultipleValuesWithEquals() {
    ArgInstruction tree = parse("ARG key1=value1 key2=value2", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 27);
    assertThat(tree.children()).hasSize(3);
    assertThat(tree.keyValuePairs()).hasSize(2);

    KeyValuePair keyValuePair1 = tree.keyValuePairs().get(0);
    assertThat(keyValuePair1)
      .hasKey("key1")
      .hasValue("value1");

    KeyValuePair keyValuePair2 = tree.keyValuePairs().get(1);
    assertThat(keyValuePair2)
      .hasKey("key2")
      .hasValue("value2");
  }
}
