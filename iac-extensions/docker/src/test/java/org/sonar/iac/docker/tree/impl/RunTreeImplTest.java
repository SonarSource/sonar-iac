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
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.RunTree;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExecFormLiteralTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.LiteralListTree;
import org.sonar.iac.docker.tree.api.SeparatedList;
import org.sonar.iac.docker.tree.api.ShellFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class RunTreeImplTest {

  @Test
  void shouldParseRunExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.RUN)
      .matches("RUN")
      .matches("RUN []")
      .matches("RUN [\"ls\"]")
      .matches("RUN [\"executable\",\"param1\",\"param2\"]")
      .matches("RUN [\"/usr/bin/wc\",\"--help\"]")
      .matches("RUN [\"/usr/bin/wc\",\"--help\"]")
      .matches("    RUN []")
      .matches("RUN [\"c:\\\\Program Files\\\\foo.exe\"]")
      .matches("run")

      .notMatches("RUND")
      // not exec form
      .notMatches("");
  }

  @Test
  void shouldParseRunShellForm() {
    Assertions.assertThat(DockerLexicalGrammar.RUN)
      .matches("RUN")
      .matches("RUN ls")
      .matches("RUN \"ls\"")
      .matches("RUN command param1 param2")
      .matches("RUN echo \"This is a test.\" | wc -")
      .matches("RUN /bin/sh /deploy.sh")
      .matches("RUN mkdir -p /output && zip -FS -r /output/lambda.zip ./")
      .matches("RUN \"/usr/bin/run.sh\"")
      .matches("    RUN \"/usr/bin/run.sh\"")
      .matches("RUN     \"/usr/bin/run.sh\"")
      .matches("run")
      // not exec form
      .matches("RUN [\"la\", \"-bb\"")
      .matches("RUN [\"la\", \"-bb]")
      .matches("RUN \"la\", \"-bb\"]")

      .notMatches("/bin/sh /deploy.sh");
  }

  @Test
  void shouldCheckParseRunExecFormTree() {
    RunTree tree = DockerTestUtils.parse("RUN [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.EXEC);
    assertThat(tree.arguments().literals().stream().map(TextTree::value)).containsExactly("\"executable\"", "\"param1\"", "\"param2\"");
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");

    assertThat(tree.children().get(1)).isInstanceOf(ExecFormTree.class);
  }

  @Test
  void shouldCheckParseRunShellFormTree() {
    RunTree tree = DockerTestUtils.parse("RUN executable param1 param2", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.SHELL);
    assertThat(tree.arguments().literals().stream().map(TextTree::value)).containsExactly("executable", "param1", "param2");

    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");

    assertThat(tree.children().get(1)).isInstanceOf(ShellFormTree.class);
  }

  @Test
  void shouldCheckParseEmptyRunExecFormTree() {
    RunTree tree = DockerTestUtils.parse("RUN []", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().literals()).isEmpty();

    assertThat(tree.children().get(1)).isInstanceOf(ExecFormTree.class);
    SeparatedList<ExecFormLiteralTree> literals = ((ExecFormTree) tree.arguments()).literalsWithSeparators();
    assertThat(literals.elementsAndSeparators()).isEmpty();
    assertThat(literals.elements()).isEmpty();
    assertThat(literals.separators()).isEmpty();
  }

  @Test
  void shouldCheckParseEmptyRunTree() {
    RunTree tree = DockerTestUtils.parse("RUN", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");

    assertThat(tree.arguments()).isNull();
  }
}
