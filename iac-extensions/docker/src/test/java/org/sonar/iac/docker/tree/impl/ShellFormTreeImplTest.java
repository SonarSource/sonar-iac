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
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ShellFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class ShellFormTreeImplTest {

  @Test
  void shouldParseExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.SHELL_FORM)
      .matches("ls")
      .matches("executable param1 param2")
      .matches("executable \"param1\" param2")
      .matches("ls    -a")
      .matches("   ls -a")
      .matches("git commit -m \"first commit\"")

      .notMatches("");
  }

  @Test
  void shouldCheckExecFormTree() {
    ShellFormTree execForm = DockerTestUtils.parse("executable param1 param2", DockerLexicalGrammar.SHELL_FORM);

    assertThat(execForm.getKind()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    List<String> elementsAndSeparatorsAsText = execForm.literals().stream()
      .map(TextTree::value)
      .collect(Collectors.toList());
    assertThat(elementsAndSeparatorsAsText).containsExactly("executable", "param1", "param2");

    List<SyntaxToken> elements = execForm.literals();
    assertThat(elements.get(0).getKind()).isEqualTo(DockerTree.Kind.TOKEN);
  }

  @Test
  void shouldCheckExecFormWithQuotesTree() {
    ShellFormTree execForm = DockerTestUtils.parse("git commit -m \"Some commit message\"", DockerLexicalGrammar.SHELL_FORM);

    assertThat(execForm.getKind()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    List<String> elementsAndSeparatorsAsText = execForm.literals().stream()
      .map(TextTree::value)
      .collect(Collectors.toList());
    assertThat(elementsAndSeparatorsAsText).containsExactly("git", "commit", "-m", "\"Some commit message\"");

    List<SyntaxToken> elements = execForm.literals();
    assertThat(elements.get(0).getKind()).isEqualTo(DockerTree.Kind.TOKEN);
  }
}
