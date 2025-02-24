/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class DecoratorImplTest extends BicepTreeModelTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.DECORATOR);

  @Test
  void shouldParseDecorator() {
    assertThat(BicepLexicalGrammar.DECORATOR)
      .matches("@functionName123()")
      .matches("@functionName123(expr, expr)")
      .matches("@member.functionName123()")
      .matches("@member.functionName123().functionName456()")
      .matches("@member.functionName123()!.functionName456()")

      .notMatches("memberExpression")
      .notMatches("@");
  }

  @Test
  void shouldParseDecoratorWithFunctionCallDetailedAssertions() {
    String code = code("@functionName123()");

    Decorator tree = (Decorator) parser.parse(code, null);
    assertThat(tree.is(ArmTree.Kind.DECORATOR)).isTrue();
    assertThat(tree.expression().is(ArmTree.Kind.FUNCTION_CALL)).isTrue();
    assertThat(tree.children()).hasSize(2);
  }

  @Test
  void shouldParseDecoratorWithMemberExpression() {
    String code = code("@member.functionName123()");

    Decorator tree = (Decorator) parser.parse(code, null);
    assertThat(tree.is(ArmTree.Kind.DECORATOR)).isTrue();
    assertThat(tree.children()).hasSize(2);

    assertThat(tree.expression().is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();
    assertThat(((MemberExpression) tree.expression()).expression().is(ArmTree.Kind.FUNCTION_CALL)).isTrue();
    assertThat(((MemberExpression) tree.expression()).memberAccess()).asWrappedIdentifier().hasValue("member");
  }

  @ParameterizedTest
  @CsvSource({
    "@foo(), true",
    "@namespace.foo(), true",
    "@decorator::foo.bar(), true",
    "@decorator!, false",
  })
  void shouldProvideAccessToDecoratorExpression(String code, boolean shouldPass) {
    Decorator tree = (Decorator) parser.parse(code, null);

    FunctionCall functionCall = tree.functionCallOrMemberFunctionCall();
    if (shouldPass) {
      assertThat(functionCall).isNotNull().isInstanceOf(FunctionCall.class);
    } else {
      assertThat(functionCall).isNull();
    }
  }
}
