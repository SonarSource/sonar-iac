/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeparatedTreesImplTest {
  private static final TerraformTree treeA = new SyntaxTokenImpl("a", null, null);
  private static final TerraformTree treeB = new SyntaxTokenImpl("b", null, null);
  private static final List<TerraformTree> elementsList = Arrays.asList(treeA, treeB);
  private static final SyntaxToken separator = new SyntaxTokenImpl(",", null, null);
  private static final SeparatedTreesImpl<TerraformTree> list = new SeparatedTreesImpl<>(elementsList, Collections.singletonList(separator));

  @Test
  void empty() {
    SeparatedTreesImpl<TerraformTree> list = SeparatedTreesImpl.empty();
    assertThat(list.trees()).isEmpty();
    assertThat(list.treesAndSeparators()).isEmpty();
  }

  @Test
  void simple_list() {
    assertThat(list.trees()).hasSize(2);
    assertThat(list.separators()).hasSize(1);
    assertThat(list.treesAndSeparators()).containsExactly(treeA, separator, treeB);
  }

  @Test
  void wrong_arguments() {
    List<TerraformTree> elements = Collections.emptyList();
    List<SyntaxToken> separators = Collections.singletonList(separator);
    assertThatThrownBy(() -> new SeparatedTreesImpl<>(elements, separators)).isInstanceOf(IllegalArgumentException.class);
  }
}
