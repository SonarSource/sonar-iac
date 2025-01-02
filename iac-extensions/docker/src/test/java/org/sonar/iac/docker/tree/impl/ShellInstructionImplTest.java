/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ShellInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ShellInstructionImplTest {
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
    ShellInstruction tree = parse("SHELL [\"executable\", \"param1\", \"param2\"]", DockerLexicalGrammar.SHELL);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.SHELL);
    assertThat(tree.keyword().value()).isEqualTo("SHELL");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 40);

    assertThat(tree.arguments().arguments().stream().map(arg -> ArgumentResolution.of(arg).value())).containsExactly("executable", "param1", "param2");
  }

  @Test
  void shellInstructionEmpty() {
    ShellInstruction tree = parse("SHELL []", DockerLexicalGrammar.SHELL);
    assertThat(tree.textRange()).hasRange(1, 0, 1, 8);

    assertThat(tree.arguments().arguments()).isEmpty();
  }
}
