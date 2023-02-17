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
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.FromInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.TestUtils.assertFrom;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class FromInstructionImplTest {

  @Test
  void test() {
    Assertions.assertThat(DockerLexicalGrammar.FROM)
      .matches("FROM foobar")
      .matches("FROM \\\n foobar")
      .matches("FROM \\\r foobar")
      .matches("FROM \\\r\n foobar")
      .matches("FROM foobar:latest")
      .matches("FROM foobar@12313423")
      .matches("FROM --platform=foo bar")
      .matches("FROM foobar AS fb")
      .matches("FROM --platform=foo bar AS fb")
      .matches("FROM foobar:latest AS fb")
      .matches("FROM ubuntu:18.04 as local-toolchain-ubuntu18.04-manylinux2010")
      .matches("FROM ${IMAGE}")
      .matches("FROM ${ROOT_CONTAINER}:${ROOT_CONTAINER_TAG}")
      .matches("FROM --platform=foo bar:latest")
      .matches("FROM --platform bar:latest")
      .matches("FROM --platform=foo bar:latest AS fb")
      .matches("FROM foobar:latest as fb")
      .matches("from foobar")
      .matches("FROM --foo=bar foobar") // No valid instruction but valid syntax -> will lead to build failure

      .notMatches("FROM foobar AS")
      .notMatches("FROM")
      .notMatches("FROM foobar foobar")
      .notMatches("FROM --platform=foo")
    ;
  }

  @Test
  void simpleImage() {
    FromInstruction from = parse("FROM foobar", DockerLexicalGrammar.FROM);
    assertThat(from.getKind()).isEqualTo(DockerTree.Kind.FROM);
    assertThat(from.keyword().value()).isEqualTo("FROM");
    assertThat(from.parent()).isNull();

    assertThat(from.image().parent()).isEqualTo(from);
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, ArgumentImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 1, 11);

    assertFrom(from, "foobar", null, null, null);
  }

  @Test
  void imageWithAlias() {
    FromInstruction from = parse("FROM foobar AS fb", DockerLexicalGrammar.FROM);
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, ArgumentImpl.class, AliasImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 1, 17);

    assertFrom(from, "foobar", null, null, "fb");
  }

  @Test
  void imageWithPlatform() {
    FromInstruction from = parse("FROM --platform=foo bar", DockerLexicalGrammar.FROM);
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, FlagImpl.class, ArgumentImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 1, 23);

    assertFrom(from, "bar", "platform", "foo", null);
  }

  @Test
  void imageWithPlatformAndAlias() {
    FromInstruction from = parse("FROM --platform=foo bar:latest AS fb", DockerLexicalGrammar.FROM);
    assertThat(from.alias()).isNotNull();
    assertThat(from.platform()).isNotNull();
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, FlagImpl.class, ArgumentImpl.class, AliasImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 1, 36);

    assertFrom(from, "bar:latest", "platform", "foo", "fb");
  }

  @Test
  void multiline() {
    FromInstruction from = parse("FROM \\\n --platform=foo \\\n bar:latest \\\n AS fb", DockerLexicalGrammar.FROM);
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, FlagImpl.class, ArgumentImpl.class, AliasImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 4, 6);

    assertFrom(from, "bar:latest", "platform", "foo", "fb");
  }

  @Test
  void multilineWindowsEOL() {
    FromInstruction from = parse("FROM \\\r\n --platform=foo \\\r\n bar:latest \\\r\n AS fb", DockerLexicalGrammar.FROM);
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, FlagImpl.class, ArgumentImpl.class, AliasImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 4, 6);

    assertFrom(from, "bar:latest", "platform", "foo", "fb");
  }
}
