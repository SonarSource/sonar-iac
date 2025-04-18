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

class ScalarTreeImplTest extends YamlTreeTest {

  @Test
  void shouldParseDoubleQuoted() {
    ScalarTree tree = parse("\"a\"", ScalarTree.class);
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.children()).isEmpty();
    assertThat(tree.metadata().tag()).isEqualTo("tag:yaml.org,2002:str");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 3);
    assertThat(tree.toHighlight()).hasRange(1, 0, 1, 3);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.DOUBLE_QUOTED);
  }

  @Test
  void shouldParseSingleQuoted() {
    ScalarTree tree = parse("'a'", ScalarTree.class);
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.children()).isEmpty();
    assertThat(tree.metadata().tag()).isEqualTo("tag:yaml.org,2002:str");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 3);
    assertThat(tree.toHighlight()).hasRange(1, 0, 1, 3);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.SINGLE_QUOTED);
  }

  @Test
  void shouldParseLiteral() {
    ScalarTree tree = parse("| \n a", ScalarTree.class);
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.children()).isEmpty();
    assertThat(tree.metadata().tag()).isEqualTo("tag:yaml.org,2002:str");
    assertThat(tree.textRange()).hasRange(1, 0, 2, 2);
    assertThat(tree.toHighlight()).hasRange(1, 0, 2, 2);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.LITERAL);
  }

  @Test
  void shouldParseFolded() {
    ScalarTree tree = parse("> \n a", ScalarTree.class);
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.children()).isEmpty();
    assertThat(tree.metadata().tag()).isEqualTo("tag:yaml.org,2002:str");
    assertThat(tree.textRange()).hasRange(1, 0, 2, 2);
    assertThat(tree.toHighlight()).hasRange(1, 0, 2, 2);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.FOLDED);
  }

  @Test
  void shouldParsePlain() {
    ScalarTree tree = parse("a", ScalarTree.class);
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.children()).isEmpty();
    assertThat(tree.metadata().tag()).isEqualTo("tag:yaml.org,2002:str");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 1);
    assertThat(tree.toHighlight()).hasRange(1, 0, 1, 1);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.PLAIN);
  }

  @Test
  void shouldParsePlainInteger() {
    ScalarTree tree = parse("123", ScalarTree.class);
    assertThat(tree.value()).isEqualTo("123");
    assertThat(tree.children()).isEmpty();
    assertThat(tree.metadata().tag()).isEqualTo("tag:yaml.org,2002:int");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 3);
    assertThat(tree.toHighlight()).hasRange(1, 0, 1, 3);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.PLAIN);
  }
}
