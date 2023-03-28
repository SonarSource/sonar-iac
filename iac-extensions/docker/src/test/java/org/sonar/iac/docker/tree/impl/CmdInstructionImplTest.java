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
import org.sonar.iac.common.testing.IacCommonAssertions;
import org.sonar.iac.common.testing.IacCommonAssertions;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExecForm;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.TestUtils.assertArgumentsValue;

class CmdInstructionImplTest {

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
      .matches("CMD \"la\", \"-bb\"]")
      .matches("CMD ${cmd}")
      .matches("CMD ${cmd:-test}")
      .matches("CMD ${cmd%%[a-z]+}")

      .notMatches("CMD [\"la\", \"-bb]")
      .notMatches("/bin/sh /deploy.sh");
  }

  @Test
  void shouldCheckParseCmdExecFormTree() {
    CmdInstruction tree = DockerTestUtils.parse("CMD [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.CMD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.CMD);
    assertThat(tree.keyword().value()).isEqualTo("CMD");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 36);

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().stream().map(t -> ArgumentResolution.of(t).value())).containsExactly("executable", "param1", "param2");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("CMD");
    assertThat(tree.children().get(1)).isInstanceOf(ExecForm.class);
  }

  @Test
  void shouldCheckParseCmdShellFormTree() {
    CmdInstruction tree = DockerTestUtils.parse("CMD executable param1 param2", DockerLexicalGrammar.CMD);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.CMD);
    assertThat(tree.keyword().value()).isEqualTo("CMD");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 28);

    assertThat(tree.arguments()).isNotNull();
    assertArgumentsValue(tree.arguments(), "executable", "param1", "param2");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("CMD");
    assertThat(tree.children().get(1)).isInstanceOf(ShellForm.class);
  }

  @Test
  void shouldCheckParseEmptyCmdExecFormTree() {
    CmdInstruction tree = DockerTestUtils.parse("CMD []", DockerLexicalGrammar.CMD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.CMD);
    assertThat(tree.keyword().value()).isEqualTo("CMD");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments()).isEmpty();
  }

  @Test
  void shouldCheckParseEmptyCmdTree() {
    CmdInstruction tree = DockerTestUtils.parse("CMD", DockerLexicalGrammar.CMD);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.CMD);
    assertThat(tree.keyword().value()).isEqualTo("CMD");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments()).isEmpty();
  }
}
