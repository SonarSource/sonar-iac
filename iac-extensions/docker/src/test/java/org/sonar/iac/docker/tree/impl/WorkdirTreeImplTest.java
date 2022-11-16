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
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.WorkdirTree;

import static org.assertj.core.api.Assertions.assertThat;

class WorkdirTreeImplTest {

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
      .notMatches("WORKDIRfooo")
      .notMatches("WORKDIR")
      .notMatches("workdir 9")
      .notMatches("WORKDIRR");
  }

  @Test
  void shouldCheckParseTree() {
    WorkdirTree tree = DockerTestUtils.parse("WORKDIR /foo bar /baz", DockerLexicalGrammar.WORKDIR);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.WORKDIR);
    assertThat(tree.instructionKeyword().value()).isEqualTo("WORKDIR");
    assertThat(tree.workdirList().stream().map(TextTree::value)).containsExactly("/foo", "bar", "/baz");
    List<SyntaxToken> children = tree.children().stream()
      .map(c -> (SyntaxToken) c)
      .collect(Collectors.toList());
    assertThat(children.get(0).value()).isEqualTo("WORKDIR");
    assertThat(children.get(1).value()).isEqualTo("/foo");
    assertThat(children.get(2).value()).isEqualTo("bar");
    assertThat(children.get(3).value()).isEqualTo("/baz");
  }
}
