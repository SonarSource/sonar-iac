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
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ShellTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ShellTreeImplTest {
  @Test
  void shouldParseShellExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.SHELL)
      .matches("SHELL []")
      .matches("shell []")
      .matches("SHELL [\"ls\"]")
      .matches("SHELL [\"executable\",\"param1\",\"param2\"]")
      .matches("SHELL [\"/usr/bin/wc\",\"--help\"]")
      .matches("SHELL [\"/usr/bin/wc\",\"--help\"]")
      .matches("    SHELL []")
      .matches("SHELL [\"c:\\\\Program Files\\\\foo.exe\"]")

      .notMatches("SHELL[]")
      .notMatches("SHELL")
      .notMatches("SHELLL []")
      .notMatches("");
  }

  @Test
  void shellInstructionExecForm() {
    ShellTree tree = parse("SHELL [\"executable\", \"param1\", \"param2\"]", DockerLexicalGrammar.SHELL);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.SHELL);
    assertThat(tree.keyword().value()).isEqualTo("SHELL");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 40);

    List<SyntaxToken> literals = tree.arguments().literals();
    assertThat(literals).hasSize(3);
    assertThat(literals.get(0).value()).isEqualTo("\"executable\"");
    assertThat(literals.get(1).value()).isEqualTo("\"param1\"");
    assertThat(literals.get(2).value()).isEqualTo("\"param2\"");
  }

  @Test
  void shellInstructionEmpty() {
    ShellTree tree = parse("SHELL []", DockerLexicalGrammar.SHELL);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 8);

    List<SyntaxToken> literals = tree.arguments().literals();
    assertThat(literals).isEmpty();
  }
}
