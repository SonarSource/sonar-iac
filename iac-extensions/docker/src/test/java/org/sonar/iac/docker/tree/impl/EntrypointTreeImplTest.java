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
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.LiteralListTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class EntrypointTreeImplTest {

  @Test
  void shouldParseEntrypointExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.ENTRYPOINT)
      .matches("ENTRYPOINT")
      .matches("ENTRYPOINT []")
      .matches("ENTRYPOINT [\"ls\"]")
      .matches("ENTRYPOINT [\"executable\",\"param1\",\"param2\"]")
      .matches("ENTRYPOINT [\"/usr/bin/wc\",\"--help\"]")
      .matches("ENTRYPOINT [\"/usr/bin/wc\",\"--help\"]")
      .matches("    ENTRYPOINT []")
      .matches("ENTRYPOINT [\"c:\\\\Program Files\\\\foo.exe\"]")
      .matches("entrypoint")

      .notMatches("ENTRYPOINTT")
      // not exec form
      .notMatches("");
  }

  @Test
  void shouldParseEntrypointShellForm() {
    Assertions.assertThat(DockerLexicalGrammar.ENTRYPOINT)
      .matches("ENTRYPOINT")
      .matches("ENTRYPOINT ls")
      .matches("ENTRYPOINT \"ls\"")
      .matches("ENTRYPOINT command param1 param2")
      .matches("ENTRYPOINT echo \"This is a test.\" | wc -")
      .matches("ENTRYPOINT /bin/sh /deploy.sh")
      .matches("ENTRYPOINT mkdir -p /output && zip -FS -r /output/lambda.zip ./")
      .matches("ENTRYPOINT \"/usr/bin/run.sh\"")
      .matches("    ENTRYPOINT \"/usr/bin/run.sh\"")
      .matches("ENTRYPOINT     \"/usr/bin/run.sh\"")
      .matches("entrypoint")
      // not exec form
      .matches("ENTRYPOINT [\"la\", \"-bb\"")
      .matches("ENTRYPOINT [\"la\", \"-bb]")
      .matches("ENTRYPOINT \"la\", \"-bb\"]")

      .notMatches("/bin/sh /deploy.sh");
  }

  @Test
  void shouldCheckParseEntrypointExecFormTree() {
    EntrypointTreeImpl tree = DockerTestUtils.parse("ENTRYPOINT [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.ENTRYPOINT);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENTRYPOINT);
    assertThat(tree.keyword().value()).isEqualTo("ENTRYPOINT");

    assertThat(tree.arguments()).isInstanceOf(ExecFormTreeImpl.class);
    assertThat(tree.arguments().literals().stream().map(TextTree::value))
      .containsExactly("\"executable\"", "\"param1\"", "\"param2\"");
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.EXEC);
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("ENTRYPOINT");
    assertThat(((ExecFormTreeImpl)tree.children().get(1))).isSameAs(tree.arguments());
  }

  @Test
  void shouldCheckParseEntrypointShellFormTree() {
    EntrypointTreeImpl tree = DockerTestUtils.parse("ENTRYPOINT executable param1 param2", DockerLexicalGrammar.ENTRYPOINT);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENTRYPOINT);
    assertThat(tree.keyword().value()).isEqualTo("ENTRYPOINT");

    assertThat(tree.arguments()).isInstanceOf(ShellFormTreeImpl.class);
    assertThat(tree.arguments().literals().stream().map(TextTree::value))
      .containsExactly("executable", "param1", "param2");
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.SHELL);
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("ENTRYPOINT");
    assertThat((tree.children().get(1))).isSameAs(tree.arguments());
  }

  @Test
  void shouldCheckParseEmptyEntrypointExecFormTree() {
    EntrypointTreeImpl tree = DockerTestUtils.parse("ENTRYPOINT []", DockerLexicalGrammar.ENTRYPOINT);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENTRYPOINT);
    assertThat(tree.keyword().value()).isEqualTo("ENTRYPOINT");

    assertThat(tree.arguments()).isInstanceOf(ExecFormTreeImpl.class);
    assertThat(tree.arguments().literals()).isEmpty();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.EXEC);
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("ENTRYPOINT");
    assertThat((tree.children().get(1))).isSameAs(tree.arguments());
  }

  @Test
  void shouldCheckParseEmptyEntrypointTree() {
    EntrypointTreeImpl tree = DockerTestUtils.parse("ENTRYPOINT", DockerLexicalGrammar.ENTRYPOINT);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENTRYPOINT);
    assertThat(tree.keyword().value()).isEqualTo("ENTRYPOINT");

    assertThat(tree.arguments()).isNull();
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("ENTRYPOINT");
    assertThat(tree.children()).hasSize(1);
  }
}
