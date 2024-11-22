/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.ArmTreeAssert;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.common.api.tree.TextTree;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class FunctionCallImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.FUNCTION_CALL);

  @Test
  void shouldParseFunctionCall() {
    ArmAssertions.assertThat(BicepLexicalGrammar.FUNCTION_CALL)
      .matches("functionName123()")
      .matches("functionName123(123)")
      .matches("functionName123(1 < 2)")
      .matches("functionName123(123, 456)")
      .matches("functionName123(1 < 2, 1 != 2)")
      .matches("functionName123(123, 456, 135)")

      .notMatches("functionName123")
      .notMatches("functionName123(")
      .notMatches("functionName123)")
      .notMatches("functionName123(,)")
      .notMatches("functionName123(,123")
      .notMatches("functionName123(123,)");
  }

  @Test
  void shouldParseFunctionCallWithDetailedAssertions() {
    String code = code("functionName123(123, 456)");

    FunctionCall tree = (FunctionCall) parser.parse(code, null);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.FUNCTION_CALL)).isTrue();
    softly.assertThat(tree.name().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    softly.assertThat(tree.name().value()).isEqualTo("functionName123");

    softly.assertThat(tree.argumentList().elements()).hasSize(2);
    softly.assertThat(tree.argumentList().separators()).hasSize(1);

    List<String> elementsAndSeparatorsAsText = tree.argumentList().elementsAndSeparators().stream()
      .map(t -> {
        if (t instanceof TextTree) {
          return ((TextTree) t).value();
        } else {
          throw new RuntimeException("Invalid cast from " + t.getClass());
        }
      })
      .collect(Collectors.toList());
    softly.assertThat(elementsAndSeparatorsAsText).containsExactly("123", ",", "456");

    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("functionName123", "(", "123", ",", "456", ")");
    softly.assertAll();
  }

  @ParameterizedTest
  @CsvSource(value = {
    "variables('foo'),VARIABLE",
    "parameters('foo'),PARAMETER",
  })
  void shouldParseFunctionCallAsVariableOrParameter(String code, ArmTree.Kind expectedKind) {
    var tree = (Expression) parser.parse(code, null);

    Assertions.assertThat(tree)
      .satisfies(t -> ArmTreeAssert.assertThat(t).is(expectedKind))
      .isInstanceOf(HasIdentifier.class)
      .extracting(t -> ((Identifier) ((HasIdentifier) t).identifier()).value())
      .isEqualTo("foo");
  }
}
