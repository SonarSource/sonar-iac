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
