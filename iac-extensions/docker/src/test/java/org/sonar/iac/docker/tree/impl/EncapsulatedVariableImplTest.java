/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EncapsulatedVariable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class EncapsulatedVariableImplTest {

  @Test
  void shouldParseEncapsulatedVariable() {
    Assertions.assertThat(DockerLexicalGrammar.ENCAPSULATED_VARIABLE)
      .matches("${foo}")
      .matches("${FOO}")
      .matches("${F2}")
      .matches("${foo:-$bar}")
      .matches("${foo:+$bar}")
      .matches("${foo:+'bar'}")
      .matches("${foo:+\"bar\"}")
      .matches("${foo:+bar}")
      .matches("${foo:+bar$bar}")
      .matches("${foo:+${bar}}")
      .matches("${foo:+${bar:-'foobar'}}")
      .matches("${foo:+${bar:-${foobar:+'foobar'}}}")
      .matches("${23}")

      .notMatches("$foo")
      .notMatches("${foo:*$bar}")
      .notMatches("${foo:+bar $bar}")
      .notMatches("${foo }")
      .notMatches("${\"foo\"}")
      .notMatches("${:-bar}")
      .notMatches("${$foo}");
  }

  @Test
  void shouldReturnCorrectNameAndDefaultValue() {
    EncapsulatedVariable variable = parse("${foo:-bar}", DockerLexicalGrammar.ENCAPSULATED_VARIABLE);

    assertThat(variable.getKind()).isEqualTo(DockerTree.Kind.ENCAPSULATED_VARIABLE);
    assertThat(variable.identifier()).isEqualTo("foo");
    assertThat(variable.modifierSeparator()).isEqualTo(":-");
    assertThat(variable.textRange()).hasRange(1, 0, 1, 11);

    Argument modifier = variable.modifier();
    assertThat(modifier).isNotNull();
    assertThat(modifier.expressions()).hasSize(1);
    assertThat(modifier.expressions().get(0)).isInstanceOfSatisfying(LiteralImpl.class,
      literal -> assertThat(literal.value()).isEqualTo("bar"));
  }

  @Test
  void shouldHaveNoModifierSeparator() {
    EncapsulatedVariable variable = parse("${foo}", DockerLexicalGrammar.ENCAPSULATED_VARIABLE);
    assertThat(variable.modifierSeparator()).isNull();
  }
}
