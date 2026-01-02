/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.yaml.tree;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.yaml.YamlTreeTest;

import static org.assertj.core.api.Assertions.assertThat;

class TupleTreeImplTest extends YamlTreeTest {

  @Test
  void testSimpleTuple() {
    TupleTree tree = parse("a: b", MappingTree.class).elements().get(0);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.metadata().tag()).isEqualTo("TUPLE");
    assertThat(tree.key()).isInstanceOfSatisfying(ScalarTree.class, k -> assertThat(k.value()).isEqualTo("a"));
    assertThat(tree.value()).isInstanceOfSatisfying(ScalarTree.class, k -> assertThat(k.value()).isEqualTo("b"));
  }

  @Test
  void shouldProvideCorrectStartAndEndMark() {
    TupleTree tree = parse("a: b", MappingTree.class).elements().get(0);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.metadata().startPointer()).isZero();
    assertThat(tree.metadata().endPointer()).isEqualTo(4);
  }

  // In below test, the use of getPointer() in YamlTreeMetadata sometimes return an unexpected value, ending with metadata that have start
  // pointer greater than end pointer. The use of getIndex() instead fix the issue.
  @Test
  void shouldProvideCorrectStartAndEndMarkForBigCode() {
    var tree = parse("""
      name: Example

      on:
        pull_request:
          branches: [ main ]

      jobs:
        main:
          runs-on: ubuntu-latest

          steps:
            # Noncompliant@+2{{Change this workflow to not use user-controlled data directly in a run block.}}
            - name: Example Step 1
              run: "echo \\"PR \\"\\"\\"\\"\\"\\"\\"title: ${{ github.event.pull_request.title }}\\""
              #                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

            # Noncompliant@+2
            - name: Example Step 2
              run: echo "PR title ${{ github.event.pull_request.title }}
              #                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

            # Noncompliant@+3
            - name: Example Step 3
              run: |
                echo "PR title: ${{ github.event.pull_request.title }}"
                something there
              #                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^@-1

            # Noncompliant@+4
            - name: Example Step 4
              run: |
                something there
                echo "PR title: ${{ github.event.head_commit.author.email }}"
                something there
              #                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^@-1
      """, MappingTree.class);

    var allTreeNodes = children(tree).toList();
    allTreeNodes.forEach(node -> {
      assertThat(node.metadata().startPointer()).isLessThanOrEqualTo(node.metadata().endPointer());
    });
  }

  Stream<YamlTree> children(YamlTree tree) {
    return Stream.concat(Stream.of(tree), tree.children()
      .stream()
      .map(YamlTree.class::cast)
      .flatMap(this::children));
  }
}
