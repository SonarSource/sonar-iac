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
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExecFormLiteralTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class ExecFormTreeImplTest {

  @Test
  void shouldParseExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.EXEC_FORM)
      .matches("[]")
      .matches("[\"ls\"]")
      .matches("[\"executable\",\"param1\",\"param2\"]")
      .matches("[\"/usr/bin/wc\",\"--help\"]")
      .matches("    [\"/usr/bin/wc\",\"--help\"]")

      .notMatches("[abc]")
      .notMatches("[\"la\" \"-bb\"")
      .notMatches("[\"la\", \"-bb\"")
      .notMatches("[\"la\", \"-bb]")
      .notMatches("\"la\", \"-bb\"]");
  }

  @Test
  void shouldCheckExecFormTree() {
    ExecFormTree execForm = DockerTestUtils.parse("[\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.EXEC_FORM);

    assertThat(execForm.getKind()).isEqualTo(DockerTree.Kind.EXEC_FORM);
    assertThat(execForm.leftBracket().value()).isEqualTo("[");
    assertThat(execForm.rightBracket().value()).isEqualTo("]");
    List<String> elementsAndSeparatorsAsText = execForm.literals().elementsAndSeparators().stream()
      .map(t -> {
        if (t instanceof SyntaxToken) {
          return ((SyntaxToken) t).value();
        } else if (t instanceof ExecFormLiteralTree) {
          return ((ExecFormLiteralTree) t).value().value();
        } else {
          throw new RuntimeException("Invalid cast from " + t.getClass());
        }
      })
      .collect(Collectors.toList());
    assertThat(elementsAndSeparatorsAsText).containsExactly("\"executable\"", "\"param1\"", "\"param2\"", ",", ",");

    List<ExecFormLiteralTree> elements = execForm.literals().elements();
    assertThat(elements.get(0).getKind()).isEqualTo(DockerTree.Kind.EXEC_FORM_LITERAL);
    assertThat(elements.stream().map(t -> t.value().value())).containsExactly("\"executable\"", "\"param1\"", "\"param2\"");

    assertThat(execForm.literals().separators().stream().map(SyntaxToken::value)).containsExactly(",", ",");
  }
}
