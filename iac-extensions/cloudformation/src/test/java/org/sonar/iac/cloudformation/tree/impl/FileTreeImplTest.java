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
package org.sonar.iac.cloudformation.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.common.extension.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

class FileTreeImplTest extends CloudformationTreeTest {

  @Test
  void file_has_a_root_tree() {
    FileTree tree = parse("a: b");
    assertThat(tree.tag()).isEqualTo("FILE");
    assertThat(tree.root()).isInstanceOf(MappingTree.class);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 4);
    assertThat(tree.textRange()).isEqualTo(tree.root().textRange());
  }

  @Test
  void empty_content_given_to_parser() {
    assertThrows(ParseException.class, () -> parse(""));
  }

  @Test
  void file_with_only_a_comment() {
    FileTree tree = parse("# foo");
    assertThat(tree.tag()).isEqualTo("FILE");
    assertThat(tree.root()).isInstanceOf(MappingTree.class);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 0);
    assertThat(tree.root().comments()).hasSize(1);
    assertThat(tree.root().comments().get(0).value()).isEqualTo("# foo");
    assertThat(tree.root().comments().get(0).contentText()).isEqualTo(" foo");
  }
}
