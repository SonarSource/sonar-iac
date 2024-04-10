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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.FunctionCall;
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
}
