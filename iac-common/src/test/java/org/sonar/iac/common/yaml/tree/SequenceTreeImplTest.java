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
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class SequenceTreeImplTest extends YamlTreeTest {

  @Test
  void shouldParseSimpleSequence() {
    SequenceTree tree = parse("[1, \"a\"]", SequenceTree.class);
    assertThat(tree.elements()).hasSize(2);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.textRange()).hasRange(1, 0, 1, 8);
    assertThat(tree.toHighlight()).hasRange(1, 1, 1, 7);
    assertThat(tree.elements().get(0)).isInstanceOfSatisfying(ScalarTree.class, e -> assertThat(e.style()).isEqualTo(ScalarTree.Style.PLAIN));
    assertThat(tree.elements().get(1)).isInstanceOfSatisfying(ScalarTree.class, e -> assertThat(e.style()).isEqualTo(ScalarTree.Style.DOUBLE_QUOTED));
    assertThat(tree.metadata().tag()).isEqualTo("tag:yaml.org,2002:seq");
  }
}
