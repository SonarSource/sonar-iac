/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class BodyImplTest {

  @Test
  void shouldFailOnEmptyBody() {
    assertThrows(ParseException.class, () -> parseBody(""), "Parse error at line 1 column 1");
  }

  @Test
  void shouldParseBodyWithFromInstruction() {
    Body body = parseBody("FROM foobar");
    assertThat(body.globalArgs()).isEmpty();
    assertThat(body.dockerImages()).hasSize(1);
  }

  @Test
  void shouldParseFileWithMultipleEmptyLinesAndInstruction() {
    Body body = parseBody("\n\n\nFROM foobar");
    assertThat(body.dockerImages()).hasSize(1);
  }

  @Test
  void shouldParseEmptyFileWithByteOrderMark() {
    Body body = parseBody("\uFEFFFROM foobar");
    assertThat(body.getKind()).isEqualTo(DockerTree.Kind.BODY);
    assertThat(body.textRange()).hasRange(1, 0, 1, 11);
    assertThat(body.globalArgs()).isEmpty();
    assertThat(body.dockerImages()).hasSize(1);
  }

  @Test
  void shouldParseFileWithMultipleEmptyLinesAndMultilineInstruction() {
    Body body = parseBody("\n\n\nFROM \\\nfoobar");
    assertThat(body.dockerImages()).hasSize(1);
  }

  @Test
  void shouldFailWithArgInstructionOnly() {
    assertThrows(ParseException.class, () -> parseBody("ARG FOO"), "Parse error at line 1 column 1");
  }

  @Test
  void shouldParseFileWithArgAndFromInstruction() {
    Body body = parseBody("ARG FOO\nFROM foobar");
    assertThat(body.globalArgs()).hasSize(1);
    assertThat(body.dockerImages()).hasSize(1);
    assertThat(body.textRange()).hasRange(1, 0, 2, 11);
  }

  @Test
  void checkIsKindMethod() {
    Body body = parseBody("FROM foobar");
    assertThat(body.is(DockerTree.Kind.BODY)).isTrue();
    assertThat(body.is(DockerTree.Kind.BODY, DockerTree.Kind.FROM)).isTrue();
    assertThat(body.is(DockerTree.Kind.FROM)).isFalse();
  }

  @Test
  void checkChildren() {
    Body body = parseBody("FROM foobar");
    assertThat(body.children()).hasExactlyElementsOfTypes(DockerImageImpl.class);
  }

  private Body parseBody(String input) {
    return parse(input, DockerLexicalGrammar.BODY);
  }
}
