/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.iac.common.testing.IacCommonAssertions;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ShellCode;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class ShellCodeTest {

  @Test
  void shouldParseShellCode() {
    Assertions.assertThat(DockerLexicalGrammar.SHELL_CODE)
      .matches(" my program")
      .matches(" anything")
      .matches(" a very long program with a lot of things inside")
      .matches("""
         <<EOF
          an heredoc content work also
        EOF""")
      .matches("""
         <<FILE1 && <<FILE2
          this also work
        FILE1
          with multiple heredoc
        FILE2""")

      .notMatches(" ")
      .notMatches("")
      .notMatches("it require an empty space at the beginning")
      .notMatches(" my\nprogram");
  }

  @Test
  void shouldParseSimpleShellCode() {
    ShellCode tree = DockerTestUtils.parse(" my program", DockerLexicalGrammar.SHELL_CODE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.SHELL_CODE);
    assertThat(tree.code()).isInstanceOfSatisfying(SyntaxToken.class, syntaxToken -> {
      assertThat(syntaxToken.value()).isEqualTo("my program");
      IacCommonAssertions.assertThat(syntaxToken.textRange()).hasRange(1, 1, 1, 11);
    });
  }
}
