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
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.DoubleQuotedString;
import org.sonar.iac.docker.tree.api.QuotedString;
import org.sonar.iac.docker.tree.api.StringLiteral;
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
    String text = "'my String'";
    Argument tree = parse(text, DockerLexicalGrammar.ARGUMENT);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARGUMENT);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 11);

    List<Tree> children = tree.children();
    assertThat(children).hasSize(1);
    QuotedString child = (QuotedString) children.get(0);
    assertThat(child.value()).isEqualTo(text);
    assertThat(child.getKind()).isEqualTo(DockerTree.Kind.QUOTED_STRING);
    assertTextRange(child.textRange()).hasRange(1, 0, 1, 11);

    List<Tree> grandchildren = child.children();
    assertThat(grandchildren).hasSize(3);
    SyntaxToken left = child.leftQuote();
    SyntaxToken leftDoubleQuote = (SyntaxToken) grandchildren.get(0);
    assertThat(left).isSameAs(leftDoubleQuote);
    assertThat(leftDoubleQuote.getKind()).isEqualTo(DockerTree.Kind.TOKEN);
    assertThat(leftDoubleQuote.value()).isEqualTo("'");
    assertTextRange(leftDoubleQuote.textRange()).hasRange(1, 0, 1, 1);

    SyntaxToken right = child.rightQuote();
    SyntaxToken rightDoubleQuote = (SyntaxToken) grandchildren.get(2);
    assertThat(right).isSameAs(rightDoubleQuote);
    assertThat(rightDoubleQuote.getKind()).isEqualTo(DockerTree.Kind.TOKEN);
    assertThat(rightDoubleQuote.value()).isEqualTo("'");
    assertTextRange(rightDoubleQuote.textRange()).hasRange(1, 10, 1, 11);

    StringLiteral stringWithSpacing = (StringLiteral) grandchildren.get(1);
    assertThat(stringWithSpacing.getKind()).isEqualTo(DockerTree.Kind.STRING_LITERAL);
    assertTextRange(stringWithSpacing.textRange()).hasRange(1, 1, 1, 10);

    List<Tree> stringWithSpacingChildren = stringWithSpacing.children();
    assertThat(stringWithSpacingChildren).hasSize(1);
    SyntaxToken stringNoSpacing = (SyntaxToken) stringWithSpacingChildren.get(0);
    assertTextRange(stringNoSpacing.textRange()).hasRange(1, 1, 1, 10);
    assertThat(stringNoSpacing.getKind()).isEqualTo(DockerTree.Kind.TOKEN);
    assertThat(stringNoSpacing.value()).isEqualTo("my String");
    assertThat(stringNoSpacing.children()).isEmpty();
  }

  @Test
  void shouldParseDoubleQuoted() {
    String text = "\"my-long-string\"";
    Argument tree = parse(text, DockerLexicalGrammar.ARGUMENT);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARGUMENT);
    assertThat(tree.value()).isEqualTo(text);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 16);

    List<Tree> children = tree.children();
    assertThat(children).hasSize(1);
    DoubleQuotedString child = (DoubleQuotedString) children.get(0);
    assertThat(child.value()).isEqualTo(text);
    assertThat(child.getKind()).isEqualTo(DockerTree.Kind.DOUBLE_QUOTED_STRING);
    assertTextRange(child.textRange()).hasRange(1, 0, 1, 16);

    List<Tree> grandchildren = child.children();
    assertThat(grandchildren).hasSize(3);
    SyntaxToken left = child.leftDoubleQuote();
    SyntaxToken leftDoubleQuote = (SyntaxToken) grandchildren.get(0);
    assertThat(left).isSameAs(leftDoubleQuote);
    assertThat(leftDoubleQuote.getKind()).isEqualTo(DockerTree.Kind.TOKEN);
    assertThat(leftDoubleQuote.value()).isEqualTo("\"");
    assertTextRange(leftDoubleQuote.textRange()).hasRange(1, 0, 1, 1);

    SyntaxToken right = child.rightDoubleQuote();
    SyntaxToken rightDoubleQuote = (SyntaxToken) grandchildren.get(2);
    assertThat(right).isSameAs(rightDoubleQuote);
    assertThat(rightDoubleQuote.getKind()).isEqualTo(DockerTree.Kind.TOKEN);
    assertThat(rightDoubleQuote.value()).isEqualTo("\"");
    assertTextRange(rightDoubleQuote.textRange()).hasRange(1, 15, 1, 16);

    StringLiteral stringWithSpacing = (StringLiteral) grandchildren.get(1);
    assertThat(stringWithSpacing.getKind()).isEqualTo(DockerTree.Kind.STRING_LITERAL);
    assertTextRange(stringWithSpacing.textRange()).hasRange(1, 1, 1, 15);

    List<Tree> stringWithSpacingChildren = stringWithSpacing.children();
    assertThat(stringWithSpacingChildren).hasSize(1);
    SyntaxToken stringNoSpacing = (SyntaxToken) stringWithSpacingChildren.get(0);
    assertTextRange(stringNoSpacing.textRange()).hasRange(1, 1, 1, 15);
    assertThat(stringNoSpacing.getKind()).isEqualTo(DockerTree.Kind.TOKEN);
    assertThat(stringNoSpacing.value()).isEqualTo("my-long-string");
    assertThat(stringNoSpacing.children()).isEmpty();
  }
}
