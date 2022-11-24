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
      .matches("COPY --option= src dest") /// ????
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
      // WHY?
//      .notMatches("COPY\\ src dest")
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
//    CopyTree test = parse("COPY--option= src dest", DockerLexicalGrammar.COPY);
//    System.out.println(test.getKind());
//

    CopyTree tree = parse("COPY src1 src2 dest", DockerLexicalGrammar.COPY);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.COPY);
    assertThat(tree.keyword().value()).isEqualTo("COPY");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 19);
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
}
