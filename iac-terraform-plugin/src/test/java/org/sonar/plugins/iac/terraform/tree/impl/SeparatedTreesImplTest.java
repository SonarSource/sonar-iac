/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeparatedTreesImplTest {
  private static Tree treeA = new InternalSyntaxToken("a", null, null);
  private static Tree treeB = new InternalSyntaxToken("b", null, null);
  private static List<Tree> elementsList = Arrays.asList(treeA, treeB);
  private static SyntaxToken separator = new InternalSyntaxToken(",", null, null);
  private static SeparatedTreesImpl<Tree> list = new SeparatedTreesImpl<>(elementsList, Arrays.asList(separator));

  @Test
  void empty() {
    SeparatedTreesImpl<Tree> list = SeparatedTreesImpl.empty();
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
    List<Tree> elements = Collections.emptyList();
    List<SyntaxToken> separators = Arrays.asList(separator);
    assertThatThrownBy(() -> new SeparatedTreesImpl<>(elements, separators)).isInstanceOf(IllegalArgumentException.class);
  }
}
