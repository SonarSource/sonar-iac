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
package org.sonar.iac.docker.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.FileTree;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class FileTreeImplTest {

  @Test
  void shouldParseEmptyFile() {
    FileTree tree = parse("", DockerLexicalGrammar.FILE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.FILE);
  }

  @Test
  void shouldParseFileWithSpace() {
    FileTree tree = parse(" ", DockerLexicalGrammar.FILE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.FILE);
  }

  @Test
  void shouldParseFileWithMultipleEmptyLines() {
    FileTree tree = parse("\n\n\n", DockerLexicalGrammar.FILE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.FILE);
  }

  @Test
  void checkIsKindMethod() {
    FileTree tree = parse("", DockerLexicalGrammar.FILE);
    assertThat(tree.is(DockerTree.Kind.FILE)).isTrue();
    assertThat(tree.is(DockerTree.Kind.FILE, DockerTree.Kind.FROM)).isTrue();
    assertThat(tree.is(DockerTree.Kind.FROM)).isFalse();
  }

  @Test
  void checkTextRange() {
    FileTree tree = parse("", DockerLexicalGrammar.FILE);
    assertThat(tree.textRange()).isEqualTo(TextRanges.range(0, 0, ""));
  }
}
