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
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.AddTree;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class AddTreeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.ADD)
      .matches("ADD src dest")
      .matches("add src dest")
      .matches("ADD dest")
      .matches("ADD src1 src2 src3 dest")
      .matches("ADD --option src dest")
      .matches("ADD --option=value src dest")
      .matches("ADD --chown=55:mygroup files* /somedir/")
      .matches("ADD --link /foo /bar")
      .matches("ADD \"src\" \"dest\"")
      .notMatches("ADD --option= src dest")
      .notMatches("ADD--option= src dest")
      .notMatches("ADDD --option= src dest")
      .notMatches("ADD")
      .notMatches("ADD ")
      .notMatches("ADD --option=value");
  }

  @Test
  void addInstructionSimple() {
    AddTree tree = parse("ADD src dest", DockerLexicalGrammar.ADD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ADD);
    assertThat(tree.keyword().value()).isEqualTo("ADD");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 12);
    assertThat(tree.options()).isEmpty();
    assertThat(tree.srcs()).hasSize(1);
    assertThat(tree.srcs().get(0).value()).isEqualTo("src");
    assertThat(tree.dest().value()).isEqualTo("dest");
  }

  @Test
  void addInstructionExecForm() {
    AddTree tree = parse("ADD [\"src\", \"dest\"]", DockerLexicalGrammar.ADD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ADD);
    assertThat(tree.keyword().value()).isEqualTo("ADD");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 19);
    assertThat(tree.options()).isEmpty();
    assertThat(tree.srcs()).hasSize(1);
    assertThat(tree.srcs().get(0).value()).isEqualTo("\"src\"");
    assertThat(tree.dest().value()).isEqualTo("\"dest\"");
  }

  @Test
  void addInstructionExecFormMultipleSrc() {
    AddTree tree = parse("ADD [\"src1\", \"src2\", \"src3\", \"dest\"]", DockerLexicalGrammar.ADD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ADD);
    assertThat(tree.keyword().value()).isEqualTo("ADD");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 36);
    assertThat(tree.options()).isEmpty();
    List<SyntaxToken> srcs = tree.srcs();
    assertThat(srcs).hasSize(3);
    assertThat(srcs.get(0).value()).isEqualTo("\"src1\"");
    assertThat(srcs.get(1).value()).isEqualTo("\"src2\"");
    assertThat(srcs.get(2).value()).isEqualTo("\"src3\"");
    assertThat(tree.dest().value()).isEqualTo("\"dest\"");
  }

  @Test
  void addInstructionSimpleQuotes() {
    AddTree tree = parse("ADD \"src\" \"dest\"", DockerLexicalGrammar.ADD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ADD);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 16);
    assertThat(tree.options()).isEmpty();
    assertThat(tree.srcs()).hasSize(1);
    assertThat(tree.srcs().get(0).value()).isEqualTo("\"src\"");
    assertThat(tree.dest().value()).isEqualTo("\"dest\"");
  }

  @Test
  void addInstructionNoSrc() {
    AddTree tree = parse("ADD dest", DockerLexicalGrammar.ADD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ADD);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 8);
    assertThat(tree.options()).isEmpty();
    assertThat(tree.srcs()).isEmpty();
    assertThat(tree.dest().value()).isEqualTo("dest");
  }

  @Test
  void addInstructionMultipleSrc() {
    AddTree tree = parse("ADD src1 src2 dest", DockerLexicalGrammar.ADD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ADD);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 18);
    assertThat(tree.options()).isEmpty();
    assertThat(tree.srcs()).hasSize(2);
    assertThat(tree.srcs().get(0).value()).isEqualTo("src1");
    assertThat(tree.srcs().get(1).value()).isEqualTo("src2");
    assertThat(tree.dest().value()).isEqualTo("dest");
  }

  @Test
  void addInstructionWithSimpleOption() {
    AddTree tree = parse("ADD --link /foo /bar", DockerLexicalGrammar.ADD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ADD);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 20);
    assertThat(tree.options()).hasSize(1);

    ParamTree option = tree.options().get(0);
    assertThat(option.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(option.name()).isEqualTo("link");
    assertThat(option.value()).isNull();
    assertThat(tree.srcs()).hasSize(1);
    assertThat(tree.srcs().get(0).value()).isEqualTo("/foo");
    assertThat(tree.dest().value()).isEqualTo("/bar");
  }

  @Test
  void addInstructionWithOption() {
    AddTree tree = parse("ADD --chown=55:mygroup files* /somedir/", DockerLexicalGrammar.ADD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ADD);
    assertThat(tree.options()).hasSize(1);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 39);

    ParamTree option = tree.options().get(0);
    assertThat(option.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(option.name()).isEqualTo("chown");
    assertThat(option.value().value()).isEqualTo("55:mygroup");

    assertThat(tree.srcs()).hasSize(1);
    assertThat(tree.srcs().get(0).value()).isEqualTo("files*");
    assertThat(tree.dest().value()).isEqualTo("/somedir/");
  }

  @Test
  void addInstructionWithMultipleOptions() {
    AddTree tree = parse("ADD --option-one=value1 --option-two=value2 src dest", DockerLexicalGrammar.ADD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ADD);
    assertThat(tree.options()).hasSize(2);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 52);

    ParamTree option1 = tree.options().get(0);
    assertThat(option1.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(option1.name()).isEqualTo("option-one");
    assertThat(option1.value().value()).isEqualTo("value1");

    ParamTree option2 = tree.options().get(1);
    assertThat(option2.getKind()).isEqualTo(DockerTree.Kind.PARAM);
    assertThat(option2.name()).isEqualTo("option-two");
    assertThat(option2.value().value()).isEqualTo("value2");

    assertThat(tree.srcs()).hasSize(1);
    assertThat(tree.srcs().get(0).value()).isEqualTo("src");
    assertThat(tree.dest().value()).isEqualTo("dest");
  }
}
