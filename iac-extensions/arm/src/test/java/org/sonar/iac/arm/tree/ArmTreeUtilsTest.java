/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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

  @Test
  void shouldParseCRLFLineSeparator() {
    var code = "resource sample 'samples/sample1.bicep' = {\r\n  params: {\r\n    location: 'westus'\r\n  }\r\n}\r\n";

    Assertions.assertThatCode(() -> BicepParser.create(BicepLexicalGrammar.FILE).parse(code)).doesNotThrowAnyException();
  }
}
