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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Docker;
import org.sonar.iac.docker.tree.api.QuotedString;
import org.sonar.iac.docker.tree.api.StringNoSpacing;
import org.sonar.iac.docker.tree.api.StringWithSpacing;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ArgumentImplTest {

  @Test
  void shouldParseArgument() {
    Assertions.assertThat(DockerLexicalGrammar.ARGUMENT)
      // quoted string
      .matches("'myString'")
      .matches("'my String'")
      .matches("'$my'")
      .matches("'${my}'")
      .matches("''")
      .matches("'${my'")
      .matches("\"WORKDIR\"")
      .matches("\"aaa\"")
      .matches("\"\"")
      .matches("\"with_\\\"double_quotes\"")
      .matches("\"with-'single-quote\"")
      .matches("\"with=escaped=\\$dollar\"")
      .matches("\"£§`~!@#%^&*()-_+=[]{}:;|\\,.<>/?\"")
      .notMatches("xx")
      .notMatches("")
      .notMatches(" ")
      .notMatches("\"");
  }

  @Test
  void shouldParseSingeQuoted() {
    Argument tree = parse("'my String'", DockerLexicalGrammar.ARGUMENT);
    assertThat(tree.getKind()).isEqualTo(Docker.Kind.ARGUMENT);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 11);

    List<Tree> children = tree.children();
    assertThat(children).hasSize(1);
    QuotedString child = (QuotedString) children.get(0);
    assertThat(child.getKind()).isEqualTo(Docker.Kind.QUOTED_STRING);
    assertTextRange(child.textRange()).hasRange(1, 0, 1, 11);

    List<Tree> grandchildren = child.children();
    assertThat(grandchildren).hasSize(1);
    SyntaxToken grandchild = (SyntaxToken) grandchildren.get(0);
    assertThat(grandchild.value()).isEqualTo("'my String'");
  }

  @Test
  void shouldParseDoubleQuoted() {
    Argument tree = parse("\"my-long-string\"'", DockerLexicalGrammar.ARGUMENT);
    assertThat(tree.getKind()).isEqualTo(Docker.Kind.ARGUMENT);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 16);

    List<Tree> children = tree.children();
    assertThat(children).hasSize(1);
    DoubleQuotedStringImpl child = (DoubleQuotedStringImpl) children.get(0);
    assertThat(child.getKind()).isEqualTo(Docker.Kind.DOUBLE_QUOTED_STRING);
    assertTextRange(child.textRange()).hasRange(1, 0, 1, 16);

    List<Tree> grandchildren = child.children();
    assertThat(grandchildren).hasSize(3);
    SyntaxToken leftDoubleQuote = (SyntaxToken) grandchildren.get(0);
    assertThat(leftDoubleQuote.getKind()).isEqualTo(Docker.Kind.TOKEN);
    assertThat(leftDoubleQuote.value()).isEqualTo("\"");
    assertTextRange(leftDoubleQuote.textRange()).hasRange(1, 0, 1, 1);

    SyntaxToken rightDoubleQuote = (SyntaxToken) grandchildren.get(2);
    assertThat(rightDoubleQuote.getKind()).isEqualTo(Docker.Kind.TOKEN);
    assertThat(rightDoubleQuote.value()).isEqualTo("\"");
    assertTextRange(rightDoubleQuote.textRange()).hasRange(1, 15, 1, 16);

    StringWithSpacing stringWithSpacing = (StringWithSpacing) grandchildren.get(1);
    assertThat(stringWithSpacing.getKind()).isEqualTo(Docker.Kind.STRING_WITH_SPACING);
    assertTextRange(stringWithSpacing.textRange()).hasRange(1, 1, 1, 15);

    List<Tree> stringWithSpacingChildren = stringWithSpacing.children();
    assertThat(stringWithSpacingChildren).hasSize(1);
    StringNoSpacing stringNoSpacing = (StringNoSpacing) stringWithSpacingChildren.get(0);
    assertTextRange(stringNoSpacing.textRange()).hasRange(1, 1, 1, 15);
    assertThat(stringNoSpacing.getKind()).isEqualTo(Docker.Kind.STRING_NO_SPACING);
    List<Tree> stringNoSpacingChildren = stringNoSpacing.children();
    assertThat(stringNoSpacingChildren).hasSize(1);

    SyntaxToken textToken = (SyntaxToken) stringNoSpacingChildren.get(0);
    assertThat(textToken.getKind()).isEqualTo(Docker.Kind.TOKEN);
    assertThat(textToken.value()).isEqualTo("my-long-string");
    assertTextRange(textToken.textRange()).hasRange(1, 1, 1, 15);
  }
}
