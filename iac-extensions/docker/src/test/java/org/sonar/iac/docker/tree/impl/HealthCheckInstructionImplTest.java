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
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.HealthCheckInstruction;
import org.sonar.iac.docker.tree.api.NoneInstruction;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.utils.ArgumentUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

class HealthCheckInstructionImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.HEALTHCHECK)
      .matches("HEALTHCHECK NONE")
      .matches("HEALTHCHECK none")
      .matches("healthcheck NONE")
      .matches("healthcheck none")
      .matches("HEALTHCHECK CMD")
      .matches("HEALTHCHECK CMD []")
      .matches("HEALTHCHECK CMD [\"ls\"]")
      .matches("HEALTHCHECK CMD command param1 param2")
      .matches("HEALTHCHECK --interval=30s CMD")
      .matches("HEALTHCHECK --interval=30s NONE")
      .matches("HEALTHCHECK --flag CMD")
      .notMatches("HEALTHCHECK")
      .notMatches("HEALTHCHECKK NONE")
      .notMatches("HEALTHCHECK --interval=30s")
      .notMatches("HEALTHCHECK NONEE")
      .notMatches("HEALTHCHECKNONE")
    ;
  }

  @Test
  void healthcheckNone() {
    HealthCheckInstruction tree = DockerTestUtils.parse("HEALTHCHECK NONE", DockerLexicalGrammar.HEALTHCHECK);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.HEALTHCHECK);
    assertThat(tree.keyword().value()).isEqualTo("HEALTHCHECK");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 16);

    assertThat(tree.isNone()).isTrue();
    assertThat(tree.instruction()).isInstanceOf(NoneInstruction.class);
    assertThat(tree.instruction().getKind()).isEqualTo(DockerTree.Kind.NONE);
    assertThat(tree.options()).isEmpty();
  }

  @Test
  void healthcheckCmd() {
    HealthCheckInstruction tree = DockerTestUtils.parse("HEALTHCHECK CMD command param", DockerLexicalGrammar.HEALTHCHECK);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 29);

    assertThat(tree.isNone()).isFalse();
    assertThat(tree.instruction()).isInstanceOf(CmdInstruction.class);
    assertThat(tree.instruction().getKind()).isEqualTo(DockerTree.Kind.CMD);
    assertThat(tree.options()).isEmpty();

    List<SyntaxToken> cmdArguments = ((CmdInstruction) tree.instruction()).arguments().literals();
    assertThat(cmdArguments).hasSize(2);
    assertThat(cmdArguments.get(0).value()).isEqualTo("command");
    assertThat(cmdArguments.get(1).value()).isEqualTo("param");
  }

  @Test
  void healthcheckCmdWithOption() {
    HealthCheckInstruction tree = DockerTestUtils.parse("HEALTHCHECK --interval=30s --timeout=5s CMD command", DockerLexicalGrammar.HEALTHCHECK);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 51);

    assertThat(tree.isNone()).isFalse();
    assertThat(tree.instruction()).isInstanceOf(CmdInstruction.class);
    assertThat(tree.options()).hasSize(2);

    List<SyntaxToken> cmdArguments = ((CmdInstruction) tree.instruction()).arguments().literals();
    assertThat(cmdArguments).hasSize(1);
    assertThat(cmdArguments.get(0).value()).isEqualTo("command");

    List<Flag> options = tree.options();
    assertThat(options.get(0).name()).isEqualTo("interval");
    assertThat(ArgumentUtils.resolve(options.get(0).value()).value()).isEqualTo("30s");
    assertThat(options.get(1).name()).isEqualTo("timeout");
    assertThat(ArgumentUtils.resolve(options.get(1).value()).value()).isEqualTo("5s");
  }
}
