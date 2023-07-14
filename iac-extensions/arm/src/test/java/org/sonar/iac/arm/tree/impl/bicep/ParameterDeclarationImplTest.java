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
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

public class ParameterDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseParameterDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.PARAMETER_DECLARATION)
      .matches("param myParam int")
      .matches("param myParam int = 123")
      .matches("param myParam int=123")
      .matches("param myParam resource 'interpolated string'")
      .matches("param myParam resource 'interpolated string' = 123")
      // invalid code that it still accepted by our parser
      .matches("param myParam int 123")
      .matches("param myParam resource interpolatedString")

      .notMatches("param")
      .notMatches("param myParam")
      .notMatches("param myParam = 123");
  }

  @Test
  void shouldParseParameterDeclarationMinimum() {
    String code = code("param myParam int");
    ParameterDeclaration tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.identifier().value()).isEqualTo("myParam");
    assertThat(tree.type()).isEqualTo(ParameterType.INT);
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree.resourceType()).isNull();
    assertThat(tree.allowedValues()).isEmpty();
    assertThat(tree.description()).isNull();
    assertThat(tree.maxLength()).isNull();
    assertThat(tree.minLength()).isNull();
    assertThat(tree.maxValue()).isNull();
    assertThat(tree.minValue()).isNull();
    assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("param", "myParam", "int");
  }

  @Test
  void shouldParseParameterDeclarationWithDefaultValue() {
    String code = code("param myParam int = 5");
    ParameterDeclaration tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.identifier().value()).isEqualTo("myParam");
    assertThat(tree.type()).isEqualTo(ParameterType.INT);
    assertThat(tree.defaultValue()).asNumericLiteral().hasValue(5);
    assertThat(tree.resourceType()).isNull();
    assertThat(tree.children()).hasSize(5);
    assertThat(tree.children()).map(token -> ((TextTree) token).value()).containsExactly("param", "myParam", "int", "=", "5");
  }

  @Test
  void shouldParseParameterDeclarationForResource() {
    String code = code("param myParam resource 'myResource'");
    ParameterDeclaration tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.identifier().value()).isEqualTo("myParam");
    assertThat(tree.type()).isNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree.resourceType().value()).isEqualTo("myResource");
    assertThat(tree.children()).map(token -> ((TextTree) token).value()).containsExactly("param", "myParam", "resource", "myResource");
  }

  @Test
  void shouldParseIntParameterDeclarationWithDecorator() {
    String code = code(
      "@description('parameter description')",
      "@minValue(0)",
      "@maxValue(10)",
      "param myParam int");
    ParameterDeclaration tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.identifier().value()).isEqualTo("myParam");
    assertThat(tree.type()).isEqualTo(ParameterType.INT);
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree.resourceType()).isNull();
    assertThat(tree.allowedValues()).isEmpty();
    assertThat(tree.description()).isNotNull().matches(s -> "parameter description".equals(s.value()));
    assertThat(tree.maxLength()).isNull();
    assertThat(tree.minLength()).isNull();
    assertThat(tree.maxValue()).isNotNull().matches(n -> "10".equals(n.value()));
    assertThat(tree.minValue()).isNotNull().matches(n -> "0".equals(n.value()));
    assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("@", "description", "(", "parameter description", ")", "@", "minValue", "(", "0", ")",
        "@", "maxValue", "(", "10", ")", "param", "myParam", "int");
  }

  @Test
  void shouldParseStringParameterDeclarationWithDecorator() {
    String code = code(
      "@description('another parameter description')",
      "@minLength(3)",
      "@maxLength(6)",
      "@allowed([",
      "  'foo'",
      "  'bar'",
      "  'foobar'",
      "])",
      "param myParam int");
    ParameterDeclaration tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.identifier().value()).isEqualTo("myParam");
    assertThat(tree.type()).isEqualTo(ParameterType.INT);
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree.resourceType()).isNull();
    assertThat(tree.allowedValues()).hasSize(3);
    assertThat(tree.description()).isNotNull().matches(s -> "another parameter description".equals(s.value()));
    assertThat(tree.maxLength()).isNotNull().matches(n -> "6".equals(n.value()));
    assertThat(tree.minLength()).isNotNull().matches(n -> "3".equals(n.value()));
    assertThat(tree.maxValue()).isNull();
    assertThat(tree.minValue()).isNull();
    assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("@", "description", "(", "another parameter description", ")", "@", "minLength", "(", "3", ")",
        "@", "maxLength", "(", "6", ")", "@", "allowed", "(", "[", "foo", "bar", "foobar", "]", ")", "param", "myParam", "int");
  }
}
