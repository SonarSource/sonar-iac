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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.HealthCheckInstruction;
import org.sonar.iac.docker.tree.api.ShellCode;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

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
      .notMatches("HEALTHCHECK--interval=30s")
      .notMatches("HEALTHCHECK NONEE")
      .notMatches("HEALTHCHECKNONE");
  }

  @Test
  void healthcheckNone() {
    HealthCheckInstruction tree = DockerTestUtils.parse("HEALTHCHECK NONE", DockerLexicalGrammar.HEALTHCHECK);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.HEALTHCHECK);
    assertThat(tree.keyword().value()).isEqualTo("HEALTHCHECK");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 16);

    assertThat(tree.isNone()).isTrue();
    assertThat(tree.none().getKind()).isEqualTo(DockerTree.Kind.TOKEN);
    assertThat(tree.none().value()).isEqualTo("NONE");
    assertThat(tree.none().textRange()).hasRange(1, 12, 1, 16);
    assertThat(tree.cmdInstruction()).isNull();
    assertThat(tree.options()).isEmpty();
  }

  @Test
  void healthcheckCmd() {
    HealthCheckInstruction tree = DockerTestUtils.parse("HEALTHCHECK CMD command param", DockerLexicalGrammar.HEALTHCHECK);
    assertThat(tree.textRange()).hasRange(1, 0, 1, 29);

    assertThat(tree.isNone()).isFalse();
    assertThat(tree.none()).isNull();
    assertThat(tree.cmdInstruction().getKind()).isEqualTo(DockerTree.Kind.CMD);
    assertThat(tree.options()).isEmpty();

    assertThat(tree.cmdInstruction().code()).isInstanceOfSatisfying(ShellCode.class,
      shellCode -> assertThat(shellCode.code()).isInstanceOfSatisfying(SyntaxToken.class, syntaxToken -> assertThat(syntaxToken.value()).isEqualTo("command param")));
  }

  @Test
  void healthcheckCmdWithOption() {
    HealthCheckInstruction tree = DockerTestUtils.parse("HEALTHCHECK --interval=30s --timeout=5s CMD command", DockerLexicalGrammar.HEALTHCHECK);
    assertThat(tree.textRange()).hasRange(1, 0, 1, 51);

    assertThat(tree.isNone()).isFalse();
    assertThat(tree.none()).isNull();
    assertThat(tree.options()).hasSize(2);

    assertThat(tree.cmdInstruction().code()).isInstanceOfSatisfying(ShellCode.class,
      shellCode -> assertThat(shellCode.code()).isInstanceOfSatisfying(SyntaxToken.class, syntaxToken -> assertThat(syntaxToken.value()).isEqualTo("command")));

    List<Flag> options = tree.options();
    assertThat(options.get(0).name()).isEqualTo("interval");
    assertThat(ArgumentResolution.of(options.get(0).value()).value()).isEqualTo("30s");
    assertThat(options.get(1).name()).isEqualTo("timeout");
    assertThat(ArgumentResolution.of(options.get(1).value()).value()).isEqualTo("5s");
  }
}
