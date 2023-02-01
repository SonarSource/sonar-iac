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
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.Docker;
import org.sonar.iac.docker.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class FileImplTest {

  @Test
  void shouldParseEmptyFile() {
    File file = parseFile("");
    assertThat(file.getKind()).isEqualTo(Docker.Kind.FILE);
    assertTextRange(file.textRange()).hasRange(1,0,1,0);
    assertThat(file.globalArgs()).isEmpty();
    assertThat(file.dockerImages()).isEmpty();
  }

  @Test
  void shouldParseEmptyFileWithByteOrderMark() {
    File file = parseFile("\uFEFF");
    assertThat(file.getKind()).isEqualTo(Docker.Kind.FILE);
    assertTextRange(file.textRange()).hasRange(1,0,1,0);
    assertThat(file.globalArgs()).isEmpty();
    assertThat(file.dockerImages()).isEmpty();
  }

  @Test
  void shouldParseFileWithSpace() {
    File file = parseFile(" ");
    assertTextRange(file.textRange()).hasRange(1,1,1,1);
    assertThat(file.globalArgs()).isEmpty();
    assertThat(file.dockerImages()).isEmpty();
  }

  @Test
  void shouldParseFileWithMultipleEmptyLines() {
    File file = parseFile("\n\n\n");
    assertTextRange(file.textRange()).hasRange(4,0,4,0);
    assertThat(file.globalArgs()).isEmpty();
    assertThat(file.dockerImages()).isEmpty();
  }

  @Test
  void shouldParseFileWithFromInstruction() {
    File file = parseFile("FROM foobar");
    assertThat(file.globalArgs()).isEmpty();
    assertThat(file.dockerImages()).hasSize(1);
  }

  @Test
  void shouldParseFileWithMultipleEmptyLinesAndInstruction() {
    File file = parseFile("\n\n\nFROM foobar");
    assertThat(file.dockerImages()).hasSize(1);
  }

  @Test
  void shouldParseFileWithMultipleEmptyLinesAndMultilineInstruction() {
    File file = parseFile("\n\n\nFROM \\\nfoobar");
    assertThat(file.dockerImages()).hasSize(1);
  }

  @Test
  void shouldParseFileWithArgInstruction() {
    File file = parseFile("ARG FOO");
    assertThat(file.globalArgs()).hasSize(1);
    assertThat(file.dockerImages()).isEmpty();
  }

  @Test
  void shouldParseFileWithArgInstructions() {
    File file = parseFile("ARG FOO\nARG BAR");
    assertThat(file.globalArgs()).hasSize(2);
    assertThat(file.dockerImages()).isEmpty();
  }

  @Test
  void shouldParseFileWithArgAndFromInstruction() {
    File file = parseFile("ARG FOO\nFROM foobar");
    assertThat(file.globalArgs()).hasSize(1);
    assertThat(file.dockerImages()).hasSize(1);
  }

  @Test
  void checkIsKindMethod() {
    File file = parseFile("");
    assertThat(file.is(Docker.Kind.FILE)).isTrue();
    assertThat(file.is(Docker.Kind.FILE, Docker.Kind.FROM)).isTrue();
    assertThat(file.is(Docker.Kind.FROM)).isFalse();
  }

  @Test
  void checkTextRange() {
    File file = parseFile("");
    assertThat(file.textRange()).isEqualTo(TextRanges.range(1, 0, ""));
  }

  @Test
  void checkChildren() {
    File file = parseFile("");
    assertThat(file.children()).hasSize(1);
    Docker child = (Docker) file.children().get(0);
    assertThat(child.getKind()).isEqualTo(Docker.Kind.TOKEN);
  }

  private File parseFile(String input) {
    return parse(input, DockerLexicalGrammar.FILE);
  }
}
