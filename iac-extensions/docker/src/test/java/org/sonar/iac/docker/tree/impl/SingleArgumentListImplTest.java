/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class SingleArgumentListImplTest {
  @Test
  void shouldWrapAnArgument() {
    Argument argument = parse("foobar", DockerLexicalGrammar.ARGUMENT);
    var singleArgumentList = new SingleArgumentListImpl(argument);

    assertThat(singleArgumentList.arguments()).containsExactly(argument);
    assertThat(singleArgumentList.children()).containsExactly(argument);
    assertThat(singleArgumentList.getKind()).isEqualTo(DockerTree.Kind.ARGUMENT);
  }
}
