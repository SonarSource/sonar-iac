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
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.ArgumentAssert;
import org.sonar.iac.docker.tree.api.CopyInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Flag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.TestUtils.assertArgumentsValue;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammarTest.FORBIDDEN_CHARACTERS_AFTER_KEYWORD;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class CopyInstructionImplTest {
  @Test
  void matchingSimple() {
    Assertions.ParserAssert copy = Assertions.assertThat(DockerLexicalGrammar.COPY)
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
      .matches("COPY ${mycopy:-test} dest")
      .notMatches("COPY <EOT\n  mkdir -p foo/bar\nEOT")
      .notMatches("COPY <<EOT\n  mkdir -p foo/bar\nEOT5")
      .notMatches("COPY--option= src dest")
      .notMatches("COPYY --option= src dest")
      .notMatches("COPY")
      .notMatches("COPY ")
      .notMatches("COPY ${mycopy%%[a-z]+} dest")
      .notMatches("COPY --option=value");

    for (char c : FORBIDDEN_CHARACTERS_AFTER_KEYWORD) {
      copy.notMatches("COPY" + c + " src dest");
    }
  }

  @Test
  void copyInstructionShellForm() {
    CopyInstruction tree = parse("COPY src1 src2 dest", DockerLexicalGrammar.COPY);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.COPY);
    assertThat(tree.keyword().value()).isEqualTo("COPY");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 19);
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.options()).isEmpty();
    assertThat(tree.srcs()).hasSize(2);

    ArgumentAssert.assertThat(tree.srcs().get(0)).hasValue("src1");
    ArgumentAssert.assertThat(tree.srcs().get(1)).hasValue("src2");
    ArgumentAssert.assertThat(tree.dest()).hasValue("dest");
  }

  @Test
  void copyInstructionExecForm() {
    CopyInstruction tree = parse("COPY [\"src1\", \"src2\", \"dest\"]", DockerLexicalGrammar.COPY);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 29);
    assertThat(tree.options()).isEmpty();
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.srcs()).hasSize(2);
    ArgumentAssert.assertThat(tree.srcs().get(0)).hasValue("src1");
    ArgumentAssert.assertThat(tree.srcs().get(1)).hasValue("src2");
    ArgumentAssert.assertThat(tree.dest()).hasValue("dest");
  }

  @Test
  void copyInstructionWithOption() {
    CopyInstruction tree = parse("COPY --chown=55:mygroup files* /somedir/", DockerLexicalGrammar.COPY);
    assertThat(tree.options()).hasSize(1);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 40);
    assertThat(tree.arguments()).isNotNull();

    Flag option = tree.options().get(0);
    assertThat(option.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(option.name()).isEqualTo("chown");
    assertThat(ArgumentResolution.of(option.value()).value()).isEqualTo("55:mygroup");

    assertThat(tree.srcs()).hasSize(1);
    ArgumentAssert.assertThat(tree.srcs().get(0)).hasValue("files*");
    ArgumentAssert.assertThat(tree.dest()).hasValue("/somedir/");
  }

  @Test
  void copyInstructionWithOptionWithoutValue() {
    // This is not valid Docker syntax but should be parsed anyway
    CopyInstruction tree = parse("COPY --option= src dest", DockerLexicalGrammar.COPY);
    assertThat(tree.options()).hasSize(1);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 23);

    Flag option = tree.options().get(0);
    assertThat(option.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(option.name()).isEqualTo("option");
    assertThat(option.value()).isNull();

    assertThat(tree.srcs()).hasSize(1);
    ArgumentAssert.assertThat(tree.srcs().get(0)).hasValue("src");
    ArgumentAssert.assertThat(tree.dest()).hasValue("dest");
  }

  @Test
  void copyInstructionHeredocForm() {
    String toParse = code("COPY <<FILE1",
      "line 1",
      "line 2",
      "FILE1");
    CopyInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.COPY);
    assertTextRange(tree.textRange()).hasRange(1,0,4,5);

    assertThat(tree.keyword().value()).isEqualTo("COPY");
    assertThat(tree.arguments()).isNotNull();
    assertArgumentsValue(tree.arguments(), "<<FILE1", "line", "1", "line", "2", "FILE1");
  }
}
