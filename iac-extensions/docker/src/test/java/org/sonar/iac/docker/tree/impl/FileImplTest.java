/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class FileImplTest {

  @Test
  void shouldFailOnEmptyFile() {
    assertThrows(ParseException.class, () -> parseFile(""), "Parse error at line 1 column 1");
  }

  @Test
  void shouldFailOnFileWithGlobalArgOnly() {
    assertThrows(ParseException.class, () -> parseFile("ARG test=val"), "Parse error at line 1 column 1");
  }

  @Test
  void shouldParseFileWithFromInstruction() {
    File file = parseFile("FROM foobar");
    assertThat(file.body().globalArgs()).isEmpty();
    assertThat(file.body().dockerImages()).hasSize(1);
  }

  @Test
  void checkIsKindMethod() {
    File file = parseFile("FROM foobar");
    assertThat(file.is(DockerTree.Kind.FILE)).isTrue();
    assertThat(file.is(DockerTree.Kind.FILE, DockerTree.Kind.FROM)).isTrue();
    assertThat(file.is(DockerTree.Kind.FROM)).isFalse();
  }

  @Test
  void checkTextRange() {
    File file = parseFile("FROM foobar");
    assertThat(file.textRange()).isEqualTo(TextRanges.range(1, 0, "FROM foobar"));
  }

  @Test
  void checkChildren() {
    File file = parseFile("FROM foobar");
    assertThat(file.children()).hasSize(2);
    assertThat(((DockerTree) file.children().get(0)).getKind()).isEqualTo(DockerTree.Kind.BODY);
    assertThat(((DockerTree) file.children().get(1)).getKind()).isEqualTo(DockerTree.Kind.TOKEN);
  }

  private File parseFile(String input) {
    return parse(input, DockerLexicalGrammar.FILE);
  }
}
