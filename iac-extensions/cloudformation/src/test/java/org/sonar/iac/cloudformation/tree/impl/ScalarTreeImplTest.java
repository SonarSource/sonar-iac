/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

class ScalarTreeImplTest extends CloudformationTreeTest {

  @Test
  void double_quoted() {
    ScalarTree tree = (ScalarTree) parse("\"a\"").root();
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.tag()).isEqualTo("tag:yaml.org,2002:str");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 3);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.DOUBLE_QUOTED);
  }

  @Test
  void single_quoted() {
    ScalarTree tree = (ScalarTree) parse("'a'").root();
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.tag()).isEqualTo("tag:yaml.org,2002:str");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 3);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.SINGLE_QUOTED);
  }

  @Test
  void literal() {
    ScalarTree tree = (ScalarTree) parse("| \n a").root();
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.tag()).isEqualTo("tag:yaml.org,2002:str");
    assertTextRange(tree.textRange()).hasRange(1, 0, 2, 2);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.LITERAL);
  }

  @Test
  void folded() {
    ScalarTree tree = (ScalarTree) parse("> \n a").root();
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.tag()).isEqualTo("tag:yaml.org,2002:str");
    assertTextRange(tree.textRange()).hasRange(1, 0, 2, 2);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.FOLDED);
  }

  @Test
  void plain() {
    ScalarTree tree = (ScalarTree) parse("a").root();
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.tag()).isEqualTo("tag:yaml.org,2002:str");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 1);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.PLAIN);
  }

  @Test
  void plain_integer() {
    ScalarTree tree = (ScalarTree) parse("123").root();
    assertThat(tree.value()).isEqualTo("123");
    assertThat(tree.tag()).isEqualTo("tag:yaml.org,2002:int");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 3);
    assertThat(tree.style()).isEqualTo(ScalarTree.Style.PLAIN);
  }
}
