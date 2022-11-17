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
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.AliasTree;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.KeyValuePairTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class FromTreeImplTest {

  @Test
  void test() {
    Assertions.assertThat(DockerLexicalGrammar.FROM)
      .matches("FROM foobar")
      .matches("FROM foobar:latest")
      .matches("FROM foobar@12313423")
      .matches("FROM --platform=foo bar")
      .matches("FROM foobar AS fb")
      .matches("FROM --platform=foo bar AS fb")
      .matches("FROM foobar:latest AS fb")
      .matches("FROM --platform=foo bar:latest")
      .matches("FROM --platform=foo bar:latest AS fb")
      .matches("FROM foobar:latest as fb")
      .matches("from foobar")

      .notMatches("FROM foobar AS")
      .notMatches("FROM")
      .notMatches("FROM foobar foobar")
      .notMatches("FROM --platform=foo")
      .notMatches("FROM --foo=bar foobar")
    ;
  }

  @Test
  void simpleImage() {
    FromTree from = parse("FROM foobar", DockerLexicalGrammar.FROM);
    assertThat(from.getKind()).isEqualTo(DockerTree.Kind.FROM);
    assertThat(from.keyword().value()).isEqualTo("FROM");
    assertThat(from.image().value()).isEqualTo("foobar");
    assertThat(from.alias()).isNull();
    assertThat(from.platform()).isNull();
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, SyntaxTokenImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 1, 11);
  }

  @Test
  void imageWithAlias() {
    FromTree from = parse("FROM foobar AS fb", DockerLexicalGrammar.FROM);
    assertThat(from.platform()).isNull();
    AliasTree alias = from.alias();
    assertThat(alias).isNotNull();
    assertThat(alias.getKind()).isEqualTo(DockerTree.Kind.ALIAS);
    assertThat(alias.alias().value()).isEqualTo("fb");
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, SyntaxTokenImpl.class, AliasTreeImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 1, 17);
  }

  @Test
  void imageWithPlatform() {
    FromTree from = parse("FROM --platform=foo bar", DockerLexicalGrammar.FROM);
    assertThat(from.alias()).isNull();
    KeyValuePairTree platform = from.platform();
    assertThat(platform).isNotNull();
    assertThat(platform.getKind()).isEqualTo(DockerTree.Kind.KEY_VALUE_PAIR);
    assertThat(platform.value().value()).isEqualTo("foo");
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, KeyValuePairTreeImpl.class, SyntaxTokenImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 1, 23);
  }

  @Test
  void imageWithPlatformAndAlias() {
    FromTree from = parse("FROM --platform=foo bar:latest AS fb", DockerLexicalGrammar.FROM);
    assertThat(from.alias()).isNotNull();
    assertThat(from.platform()).isNotNull();
    assertThat(from.children()).hasExactlyElementsOfTypes(SyntaxTokenImpl.class, KeyValuePairTreeImpl.class, SyntaxTokenImpl.class, AliasTreeImpl.class);
    assertTextRange(from.textRange()).hasRange(1, 0, 1, 36);
  }
}
