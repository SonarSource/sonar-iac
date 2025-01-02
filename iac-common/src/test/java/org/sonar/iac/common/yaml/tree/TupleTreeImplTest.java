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
package org.sonar.iac.common.yaml.tree;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.yaml.YamlTreeTest;

import static org.assertj.core.api.Assertions.assertThat;

class TupleTreeImplTest extends YamlTreeTest {

  @Test
  void simple_tuple() {
    TupleTree tree = parse("a: b", MappingTree.class).elements().get(0);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.metadata().tag()).isEqualTo("TUPLE");
    assertThat(tree.key()).isInstanceOfSatisfying(ScalarTree.class, k -> assertThat(k.value()).isEqualTo("a"));
    assertThat(tree.value()).isInstanceOfSatisfying(ScalarTree.class, k -> assertThat(k.value()).isEqualTo("b"));
  }
}
