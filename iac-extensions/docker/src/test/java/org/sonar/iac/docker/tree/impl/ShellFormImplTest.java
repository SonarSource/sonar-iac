/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EncapsulatedVariable;
import org.sonar.iac.docker.tree.api.ShellForm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.DockerAssertions.assertThat;
import static org.sonar.iac.docker.TestUtils.assertArgumentsValue;

class ShellFormImplTest {

  @Test
  void shouldParseShellForm() {
    Assertions.assertThat(DockerLexicalGrammar.SHELL_FORM)
      .matches(" ls")
      .matches(" executable param1 param2")
      .matches(" executable \"param1\" param2")
      .matches(" ls    -a")
      .matches(" git commit -m \"first commit\"")
      .matches(" $var")
      .matches(" ${var}")
      .matches(" ${var:-test}")

      .notMatches(" ${var%%[a-z+]}")
      .notMatches("ls -a")
      .notMatches("");
  }

  @Test
  void shouldParseShellFormGeneric() {
    Assertions.assertThat(DockerLexicalGrammar.SHELL_FORM_GENERIC)
      .matches(" ls")
      .matches(" executable param1 param2")
      .matches(" executable \"param1\" param2")
      .matches(" ls    -a")
      .matches(" git commit -m \"first commit\"")
      .matches(" $var")
      .matches(" ${var}")
      .matches(" ${var:-test}")
      .matches(" ${var%%[a-z+]}")

      .notMatches("ls -a")
      .notMatches("");
  }

  @Test
  void shouldCheckShellFormTree() {
    ShellForm shellForm = DockerTestUtils.parse(" executable param1 param2", DockerLexicalGrammar.SHELL_FORM);

    assertThat(shellForm.getKind()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    assertArgumentsValue(shellForm.arguments(), "executable", "param1", "param2");
  }

  @Test
  void shouldCheckShellFormTreeWithVariable() {
    ShellForm shellForm = DockerTestUtils.parse(" executable $var", DockerLexicalGrammar.SHELL_FORM);

    assertThat(shellForm.getKind()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    assertArgumentsValue(shellForm.arguments(), "executable", "");
  }

  @Test
  void shouldCheckShellFormWithQuotesTree() {
    ShellForm shellForm = DockerTestUtils.parse(" git commit -m \"Some commit message\"", DockerLexicalGrammar.SHELL_FORM);

    assertThat(shellForm.getKind()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    assertArgumentsValue(shellForm.arguments(), "git", "commit", "-m", "Some commit message");
  }

  @Test
  void shouldCheckShellFormTreeGenericVariable() {
    ShellForm shellForm = DockerTestUtils.parse(" ${var%%[a-z]+}", DockerLexicalGrammar.SHELL_FORM_GENERIC);
    assertThat(shellForm.getKind()).isEqualTo(DockerTree.Kind.SHELL_FORM);

    assertThat(shellForm.arguments()).hasSize(1);
    assertThat(shellForm.arguments().get(0).expressions()).hasSize(1);
    EncapsulatedVariable var = (EncapsulatedVariable) shellForm.arguments().get(0).expressions().get(0);
    assertThat(var.identifier()).isEqualTo("var");
    assertThat(var.modifierSeparator()).isNull();
    assertThat(var.modifier()).hasValue("%%[a-z]+");
  }
}
