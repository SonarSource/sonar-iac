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
import org.sonar.iac.docker.tree.api.CmdTree;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class CmdTreeImplTest {

  @Test
  void shouldParseCmdExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.CMD)
      .matches("CMD")
      .matches("CMD []")
      .matches("CMD [\"ls\"]")
      .matches("CMD [\"executable\",\"param1\",\"param2\"]")
      .matches("CMD [\"/usr/bin/wc\",\"--help\"]")
      .matches("CMD [\"/usr/bin/wc\",\"--help\"]")
      .matches("    CMD []")
      .matches("CMD [\"c:\\\\Program Files\\\\foo.exe\"]")
      .matches("cmd")

      .notMatches("CMDD")
      // not exec form
      .notMatches("");
  }

  @Test
  void shouldParseCmdShellForm() {
    Assertions.assertThat(DockerLexicalGrammar.CMD)
      .matches("CMD")
      .matches("CMD ls")
      .matches("CMD \"ls\"")
      .matches("CMD command param1 param2")
      .matches("CMD echo \"This is a test.\" | wc -")
      .matches("CMD /bin/sh /deploy.sh")
      .matches("CMD mkdir -p /output && zip -FS -r /output/lambda.zip ./")
      .matches("CMD \"/usr/bin/run.sh\"")
      .matches("    CMD \"/usr/bin/run.sh\"")
      .matches("CMD     \"/usr/bin/run.sh\"")
      .matches("cmd")
      // not exec form
      .matches("CMD [\"la\", \"-bb\"")
      .matches("CMD [\"la\", \"-bb]")
      .matches("CMD \"la\", \"-bb\"]")

      .notMatches("/bin/sh /deploy.sh");
  }

  @Test
  void shouldCheckParseCmdExecFormTree() {
    CmdTree tree = DockerTestUtils.parse("CMD [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.CMD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.CMD);
    assertThat(tree.keyword().value()).isEqualTo("CMD");

    assertThat(tree.cmdArguments().stream().map(TextTree::value)).containsExactly("\"executable\"", "\"param1\"", "\"param2\"");
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("CMD");

    ExecFormTree execForm = (ExecFormTree) tree.children().get(1);
    assertThat(execForm).isSameAs(tree.execForm());
  }

  @Test
  void shouldCheckParseEmptyCmdExecFormTree() {
    CmdTree tree = DockerTestUtils.parse("CMD []", DockerLexicalGrammar.CMD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.CMD);
    assertThat(tree.keyword().value()).isEqualTo("CMD");

    assertThat(tree.cmdArguments()).isEmpty();
  }
}
