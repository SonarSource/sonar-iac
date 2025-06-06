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
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class ParameterDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseParameterDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.PARAMETER_DECLARATION)
      .matches("param myParam int")
      .matches("param myParam int = 123")
      .matches("param myParam int=123")
      .matches("param myParam resource 'interpolated string'")
      .matches("param myParam resource 'interpolated string' = 123")
      .matches("param utcValue string = utcNow()")
      .matches("param location string = deployment().location")
      .matches("param appServiceAppName string = 'app-contoso-${environmentName}-${uniqueString(resourceGroup().id)}'")
      .matches("param identity string")
      .matches("param utcValue string = utcNow()")
      .matches("param storageAccountName string = 'mystore'")
      .matches("param propertyDeref anObject.property = 10")
      .matches("param itemDeref tuple[1] = 'baz'")
      .matches("param foo string?")
      .matches("param foo int?")
      // The stringArrayType needs to be defined like: type stringArrayType = string[]
      .matches("param param1 stringArrayType[*] = 'bar'")
      .matches("param param2 stringArrayType = ['bar']")
      .matches("param param3 stringArrayType[*]?")
      .matches("param param4 stringArrayType[*][] = ['bar']")
      .matches("param param5 stringArrayType[*][]?")
      // type typeWithAdditionalProperties = { *: string }
      .matches("param param6 typeWithAdditionalProperties.*")
      // type typeWithAdditionalPropertiesArray = { *: string[] }
      .matches("param param7 typeWithAdditionalPropertiesArray.*[*]")
      // Type definitions needed for next examples
      // type fruit = 'apple' | 'banana'
      // type fruitQuantity = [fruit, int]
      // type basket = { *: fruitQuantity[] }
      .matches("param param9 fruitQuantity[1] = 1")
      .matches("param paramA fruitQuantity[0] = 'apple'")
      .matches("param paramA fruitQuantity[0]? = 'apple'")
      .matches("param param8 basket.*[*][0]")
      .matches("param param8 basket.*[*][0]")
      .matches("param param9 basket.*[*][]")
      .matches("param paramA basket.*[*]")
      .matches("param paramB basket.*[]")
      .matches("param paramC basket.*")
      .matches("param paramD basket.*[*][0]?")
      .matches("param paramE basket.*[*][]?")
      .matches("param paramF basket.*[*]?")
      .matches("param paramG basket.*[]?")
      .matches("param paramH basket.*?")
      .matches("param paramK (stringArrayType[*]?)[]")
      .matches("param paramN (stringArrayType?)[]")

      // defining a param of name the same as keyword is possible
      .matches("param type int = 123")
      .matches("param if int = 123")
      .matches("param for int = 123")
      .matches("param param int = 123")
      // invalid code that it still accepted by our parser
      .matches("param myParam resource 'interpolatedString'")

      .notMatches("param")
      .notMatches("param myParam")
      .notMatches("param myParam = 123")
      .notMatches("param myParam int 123")
      .notMatches("param identity string param")
      .notMatches("@decorator[] param myParam = 123")
      // The ? should be last or in
      .notMatches("param param6 stringArrayType[*]?[]")
      .notMatches("param param7 stringArrayType?[*][]")
      .notMatches("param param8 stringArrayType?[*]")
      .notMatches("param param9 stringArrayType?[]")
      // Error BCP391: Type member access is only supported on a reference to a named type.
      .notMatches("param paramL ((stringArrayType?)[*])[]")
      .notMatches("param paramM (stringArrayType?)[*]");
  }

  @Test
  void shouldParseParameterDeclarationMinimum() {
    String code = "param myParam int";
    ParameterDeclarationImpl tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("myParam");
    assertThat(tree.type()).isEqualTo(ParameterType.INT);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.typeExpression())).containsExactly("int");
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree.resourceType()).isNull();
    assertThat(tree.allowedValues()).isEmpty();
    assertThat(tree.description()).isNull();
    assertThat(tree.maxLength()).isNull();
    assertThat(tree.minLength()).isNull();
    assertThat(tree.maxValue()).isNull();
    assertThat(tree.minValue()).isNull();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("param", "myParam", "int");
  }

  @Test
  void shouldParseParameterDeclarationWithDefaultValue() {
    String code = "param myParam int = 5";
    ParameterDeclarationImpl tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("myParam");
    assertThat(tree.type()).isEqualTo(ParameterType.INT);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.typeExpression())).containsExactly("int");
    assertThat(tree.defaultValue()).asNumericLiteral().hasValue(5);
    assertThat(tree.resourceType()).isNull();
    assertThat(tree.children()).hasSize(5);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("param", "myParam", "int", "=", "5");
  }

  @Test
  void shouldParseParameterDeclarationForResource() {
    String code = "param myParam resource 'myResource'";
    ParameterDeclarationImpl tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("myParam");
    assertThat(tree.type()).isNull();
    assertThat(tree.typeExpression()).isNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree.resourceType().value()).isEqualTo("myResource");
    assertThat(tree.children()).map(token -> ((TextTree) token).value()).containsExactly("param", "myParam", "resource", "myResource");
  }

  @Test
  void shouldParseIntParameterDeclarationWithDecorator() {
    String code = """
      @description('parameter description')
      @minValue(0)
      @maxValue(10)
      param myParam int""";
    ParameterDeclarationImpl tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("myParam");
    assertThat(tree.type()).isEqualTo(ParameterType.INT);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.typeExpression())).containsExactly("int");
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree.resourceType()).isNull();
    assertThat(tree.allowedValues()).isEmpty();
    assertThat(tree.description()).isNotNull().matches(s -> "parameter description".equals(s.value()));
    assertThat(tree.maxLength()).isNull();
    assertThat(tree.minLength()).isNull();
    assertThat(tree.maxValue()).isNotNull().matches(n -> "10".equals(n.value()));
    assertThat(tree.minValue()).isNotNull().matches(n -> "0".equals(n.value()));
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("@", "description", "(", "parameter description", ")", "@", "minValue", "(", "0", ")",
        "@", "maxValue", "(", "10", ")", "param", "myParam", "int");
  }

  @Test
  void shouldParseStringParameterDeclarationWithDecorator() {
    String code = """
      @description('another parameter description')
      @sys.minLength(3)
      @sys.maxLength(6)
      @allowed([
        'foo'
        'bar'
        'foobar'
      ])
      param myParam int""";
    ParameterDeclarationImpl tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.PARAMETER_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("myParam");
    assertThat(tree.type()).isEqualTo(ParameterType.INT);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.typeExpression())).containsExactly("int");
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree.resourceType()).isNull();
    assertThat(tree.allowedValues()).hasSize(3);
    assertThat(tree.description()).isNotNull().matches(s -> "another parameter description".equals(s.value()));
    assertThat(tree.maxLength()).isNotNull().matches(n -> "6".equals(n.value()));
    assertThat(tree.minLength()).isNotNull().matches(n -> "3".equals(n.value()));
    assertThat(tree.maxValue()).isNull();
    assertThat(tree.minValue()).isNull();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("@", "description", "(", "another parameter description", ")", "@", "sys", ".", "minLength",
        "(", "3", ")", "@", "sys", ".", "maxLength", "(", "6", ")", "@", "allowed", "(", "[", "foo", "bar", "foobar", "]", ")",
        "param", "myParam", "int");
  }

  @Test
  void accessorsWithInvalidDecorators() {
    String code = """
      @description(44)
      @minValue('0')
      @maxValue([10])
      @allowed(0)
      param myParam int""";
    ParameterDeclaration tree = parse(code, BicepLexicalGrammar.PARAMETER_DECLARATION);

    assertThatThrownBy(tree::description).isInstanceOf(ClassCastException.class);
    assertThatThrownBy(tree::maxValue).isInstanceOf(ClassCastException.class);
    assertThatThrownBy(tree::minValue).isInstanceOf(ClassCastException.class);
  }
}
