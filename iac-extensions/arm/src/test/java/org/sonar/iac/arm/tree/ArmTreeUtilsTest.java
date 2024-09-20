/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.tree;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;

class ArmTreeUtilsTest {
  @Test
  void shouldReturnFunctionNameForFunctionCall() {
    var code = "foo()";
    var expression = (Expression) BicepParser.create(BicepLexicalGrammar.EXPRESSION).parse(code);
    var functionName = ArmTreeUtils.functionCallNameOrNull(expression);

    Assertions.assertThat(functionName)
      .isNotNull()
      .extracting(Identifier::value)
      .isEqualTo("foo");
  }

  @Test
  void shouldReturnNullForNonFunctionCall() {
    var code = "bar";
    var expression = (Expression) BicepParser.create(BicepLexicalGrammar.EXPRESSION).parse(code);
    var functionName = ArmTreeUtils.functionCallNameOrNull(expression);

    Assertions.assertThat(functionName).isNull();
  }
}
