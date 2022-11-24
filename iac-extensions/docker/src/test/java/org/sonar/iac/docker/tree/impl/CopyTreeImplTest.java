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
import org.sonar.iac.docker.tree.api.CopyTree;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.LiteralListTree;
import org.sonar.iac.docker.tree.api.ParamTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class CopyTreeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.COPY)
      .matches("COPY src dest")
      .matches("copy src dest")
      .matches("COPY dest")
      .matches("COPY . /opt/foo")
      .matches("COPY . .")
      .matches("COPY src1 src2 src3 dest")
      .matches("COPY [\"src1\", \"src2\", \",src3\", \"dest\"]")
      .matches("COPY --option src dest")
      .matches("COPY --option=value src dest")
      .matches("COPY --chown=55:mygroup files* /somedir/")
      .matches("COPY --link /foo /bar")
      .matches("COPY    --link    /foo     /bar")
      .matches("COPY \"src\" \"dest\"")
      .matches("COPY --option= src dest")
      .matches("COPY <<EOT\n  mkdir -p foo/bar\nEOT")
      .matches("COPY <<EOT early code\n  mkdir -p foo/bar\nEOT")
      .matches("   COPY       <<EOT\n  mkdir -p foo/bar\nEOT")
      .matches("COPY <<eot\n  mkdir -p foo/bar\neot")
      .matches("COPY <<EOT\n\n  mkdir -p foo/bar\n\n\nEOT")
      .matches("COPY <<EOT\r\n  mkdir -p foo/bar\r\nEOT")
      .matches("COPY <<EOT1 <<EOT2\n  mkdir -p foo/bar\nEOT1\n  mkdir -p foo/bar\nEOT2")
      .matches("COPY <<-EOT\n  mkdir -p foo/bar\nEOT")
      .matches("COPY <<\"EOT\"\n  mkdir -p foo/bar\nEOT")
      .notMatches("COPY <EOT\n  mkdir -p foo/bar\nEOT")
      .notMatches("COPY <<EOT\n  mkdir -p foo/bar\nEOT5")
      .notMatches("COPY --option= src dest")
      .notMatches("COPY--option= src dest")
      .notMatches("COPY___ src dest")
      .notMatches("COPY< src dest")
      .notMatches("COPY> src dest")
      .notMatches("COPY123 src dest")
      .notMatches("COPY! src dest")
      .notMatches("COPY@ src dest")
      .notMatches("COPY# src dest")
      .notMatches("COPY$ src dest")
      .notMatches("COPY% src dest")
      .notMatches("COPY& src dest")
      .notMatches("COPY£ src dest")
      .notMatches("COPY§ src dest")
      .notMatches("COPY` src dest")
      .notMatches("COPY~ src dest")
      .notMatches("COPY* src dest")
      .notMatches("COPY( src dest")
      .notMatches("COPY) src dest")
      .notMatches("COPY= src dest")
      .notMatches("COPY{ src dest")
      .notMatches("COPY} src dest")
      .notMatches("COPY[ src dest")
      .notMatches("COPY] src dest")
      .notMatches("COPY\" src dest")
      .notMatches("COPY' src dest")
      .notMatches("COPY: src dest")
      .notMatches("COPY; src dest")
      .notMatches("COPY| src dest")
      .notMatches("COPY/ src dest")
      .notMatches("COPY? src dest")
      .notMatches("COPY. src dest")
      .notMatches("COPY, src dest")
      .notMatches("COPYY --option= src dest")
      .notMatches("COPY")
      .notMatches("COPY ")
      .notMatches("COPY --option=value");
  }

  @Test
  void copyInstructionShellForm() {
    CopyTree tree = parse("COPY src1 src2 dest", DockerLexicalGrammar.COPY);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.COPY);
    assertThat(tree.keyword().value()).isEqualTo("COPY");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 19);
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.SHELL);
    assertThat(tree.options()).isEmpty();
    assertThat(tree.srcs()).hasSize(2);
    assertThat(tree.srcs().get(0).value()).isEqualTo("src1");
    assertThat(tree.srcs().get(1).value()).isEqualTo("src2");
    assertThat(tree.dest().value()).isEqualTo("dest");
  }

  @Test
  void copyInstructionExecForm() {
    CopyTree tree = parse("COPY [\"src1\", \"src2\", \"dest\"]", DockerLexicalGrammar.COPY);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 29);
    assertThat(tree.options()).isEmpty();
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.EXEC);
    assertThat(tree.srcs()).hasSize(2);
    assertThat(tree.srcs().get(0).value()).isEqualTo("\"src1\"");
    assertThat(tree.srcs().get(1).value()).isEqualTo("\"src2\"");
    assertThat(tree.dest().value()).isEqualTo("\"dest\"");
  }

  @Test
  void copyInstructionWithOption() {
    CopyTree tree = parse("COPY --chown=55:mygroup files* /somedir/", DockerLexicalGrammar.COPY);
    assertThat(tree.options()).hasSize(1);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 40);
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.SHELL);

    ParamTree option = tree.options().get(0);
    assertThat(option.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(option.name()).isEqualTo("chown");
    assertThat(option.value().value()).isEqualTo("55:mygroup");

    assertThat(tree.srcs()).hasSize(1);
    assertThat(tree.srcs().get(0).value()).isEqualTo("files*");
    assertThat(tree.dest().value()).isEqualTo("/somedir/");
  }

  @Test
  void copyInstructionWithOptionWithoutValue() {
    // This is not valid Docker syntax but should be parsed anyway
    CopyTree tree = parse("COPY --option= src dest", DockerLexicalGrammar.COPY);
    assertThat(tree.options()).hasSize(1);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 23);

    ParamTree option = tree.options().get(0);
    assertThat(option.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(option.name()).isEqualTo("option");
    assertThat(option.value()).isNull();

    assertThat(tree.srcs()).hasSize(1);
    assertThat(tree.srcs().get(0).value()).isEqualTo("src");
    assertThat(tree.dest().value()).isEqualTo("dest");
  }

  void copyInstructionHeredocForm() {
    String toParse = "COPY <<FILE1\n" +
      "line 1\n" +
      "line 2\n" +
      "FILE1";
    CopyTree tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.COPY);
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.HEREDOC);
    assertThat(tree.arguments().getKind()).isEqualTo(DockerTree.Kind.HEREDOCUMENT);
    assertTextRange(tree.textRange()).hasRange(1,0,4,5);

    assertThat(tree.keyword().value()).isEqualTo("COPY");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.HEREDOC);
    assertThat(tree.arguments().literals()).hasSize(1);
    assertThat(tree.arguments().literals().get(0).value()).isEqualTo("<<FILE1\nline 1\nline 2\nFILE1");
  }
}
