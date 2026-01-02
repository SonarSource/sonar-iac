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
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class RegularVariableImplTest {

  @Test
  void shouldParseRegularVariable() {
    Assertions.assertThat(DockerLexicalGrammar.REGULAR_VARIABLE)
      .matches("$foo")
      .matches("$FOO")
      .matches("$F1")
      .matches("$_foo")
      .matches("$1")

      .notMatches("$foo=")
      .notMatches("$foo-bar")
      .notMatches("$foo.bar");
  }

  @Test
  void shouldCheckEquality() {
    RegularVariableImpl variable1 = parse("$foo", DockerLexicalGrammar.REGULAR_VARIABLE);
    RegularVariableImpl variable2 = parse("$foo", DockerLexicalGrammar.REGULAR_VARIABLE);
    RegularVariableImpl variable3 = parse("$bar", DockerLexicalGrammar.REGULAR_VARIABLE);

    assertThat(variable1)
      .isEqualTo(variable1)
      .isEqualTo(variable2)
      .hasSameHashCodeAs(variable2)
      .isNotEqualTo(variable3)
      .doesNotHaveSameHashCodeAs(variable3)
      .isNotEqualTo(null)
      .isNotEqualTo(new Object());
  }
}
