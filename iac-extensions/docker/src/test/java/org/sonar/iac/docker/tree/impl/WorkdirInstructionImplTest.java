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
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.WorkdirInstruction;

import static org.assertj.core.api.Assertions.assertThat;

class WorkdirInstructionImplTest {

  @Test
  void shouldParseWorkdir() {
    Assertions.assertThat(DockerLexicalGrammar.WORKDIR)
      .matches("WORKDIR foo")
      .matches("WORKDIR /path/to/workdir")
      .matches("WORKDIR \"bar\"")
      .matches("WORKDIR /foo")
      .matches("WORKDIR \"/foo\"")
      .matches("WORKDIR 1")
      .matches("WORKDIR 123")
      .matches("WORKDIR c:\\\\windows")
      .matches("WORKDIR c:/windows")
      .matches("WORKDIR c:\\\\Program Files\\\\foo")
      .matches("WORKDIR \"c:\\\\Program Files\\\\foo\"")
      .matches("WORKDIR \"c:\\\\Program\\ Files\\\\foo\"")
      .matches("WORKDIR /foo /bar /biz")
      .matches("WORKDIR foo bar biz")
      .matches("   WORKDIR 1")
      .matches("workdir 9")
      .matches("wOrKdir 9")
      .notMatches("WORKDIRfooo")
      .notMatches("WORKDIR")
      .notMatches("WORKDIRR");
  }

  @Test
  void shouldCheckParseTree() {
    WorkdirInstruction tree = DockerTestUtils.parse("WORKDIR /foo bar /baz", DockerLexicalGrammar.WORKDIR);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.WORKDIR);
    assertThat(tree.keyword().value()).isEqualTo("WORKDIR");
    assertThat(tree.arguments().stream().map(arg -> ArgumentResolution.of(arg).value())).containsExactly("/foo", "bar", "/baz");
    assertThat(tree.children().get(0)).isInstanceOf(SyntaxToken.class);
    assertThat(tree.children().get(1)).isInstanceOf(Argument.class);
    assertThat(tree.children().get(2)).isInstanceOf(Argument.class);
    assertThat(tree.children().get(3)).isInstanceOf(Argument.class);
  }

  @Test
  void test() {
    WorkdirInstruction tree = DockerTestUtils.parse("WORKDIR executable \"param1 ${my_var:-test}\"", DockerLexicalGrammar.WORKDIR);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.WORKDIR);
  }
}
