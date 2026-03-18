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
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.CompoundTypeReference;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.ResourceDerivedType;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TypeExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class TypeDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseTypeDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.TYPE_DECLARATION)
      .matches("type myType=abc")
      .matches("type myType= abc")
      .matches("type myType =abc")
      .matches("type myType = abc")
      .matches("type myType = bool[] | int?")
      .matches("type myType = bool")
      // defining a type of name the same as keyword is possible
      .matches("type type = bool")
      .matches("type if = bool")
      // also it can be used after equal sign
      .matches("type t1 = type")
      .matches("type t2 = var")
      .matches("type t3 = if")
      .matches("type t4 = func")
      .matches("type t5 = for")
      .matches("type t6 = param")
      .matches("type t7 = output")
      .matches("type t1 = { type : var }")
      .matches("type t2 = { var : if }")
      .matches("type t3 = { if : func }")
      .matches("@description('my type') type myType = abc")
      .matches("@sys.description('my type') type myType = abc")
      .matches("@sys.description('my type') type myType = bool[] | int?")
      .matches("""
        @description('my type')
        @decorator()
        type myType = abc""")
      .matches("type fooProperty = foo.objectProp.intProp")
      .matches("type fooProperty = foo.object_prop.intProp")
      .matches("type fooProperty = foo.intProp123")
      .matches("""
        type test = {
          baz: types.myObject
        }""")
      .matches("""
        type test = {
          property: string?
        }""")
      .matches("type type1 = basket.*[*][0]")
      .matches("type type2 = basket.*[*][]")
      .matches("type type3 = basket.*[*]")
      .matches("type type4 = basket.*[]")
      .matches("type type5 = basket.*")
      .matches("type type6 = basket.*[*][0]?")
      .matches("type type7 = basket.*[*][]?")
      .matches("type type8 = basket.*[*]?")
      .matches("type type9 = basket.*[]?")
      .matches("type typeA = basket.*?")
      .matches("type myType1 = (string?)[]")
      .matches("type myType2 = (basket.*[*][0])[]")
      .matches("type myType3 = (basket.*[*][])[]")
      .matches("type myType4 = (basket.*[*])[]")
      .matches("type myType5 = (basket.*[])[]")
      .matches("type myType6 = (basket.*)[]")
      .matches("type myType7 = (basket.*[*][0]?)[]")
      .matches("type myType8 = (basket.*[*][]?)[]")
      .matches("type myType9 = (basket.*[*]?)[]")
      .matches("type myTypeA = (basket.*[]?)[]")
      .matches("type myTypeB = (basket.*)[]")

      // non-null assertion on type references
      .matches("type nonNullable = nullPrefixedName")
      .matches("type nonNullable = nullable!")
      .matches("type t = foo!.a.b!")
      .matches("type t = foo!.bar")

      // resource-derived types
      .matches("type accountKind = resourceInput<'Microsoft.Storage/storageAccounts@2024-01-01'>.kind")
      .matches("type storageProps = resourceInput<'Microsoft.Storage/storageAccounts@2023-01-01'>.properties")
      .matches("type endpoints = resourceOutput<'Microsoft.Storage/storageAccounts@2024-01-01'>.properties.primaryEndpoints")
      .matches("type myType = resourceInput<'Microsoft.Storage/storageAccounts@2024-01-01'>")
      .matches("type myType = resourceOutput<'Microsoft.Network/virtualNetworks@2023-04-01'>.properties")
      .matches("type myType = sys.resourceOutput<'Microsoft.Network/virtualNetworks@2023-04-01'>.properties")
      .matches("type myType = sys.resourceInput<'Microsoft.Storage/storageAccounts@2024-01-01'>")
      .matches("type myType = notASys.resourceInput<'Microsoft.Storage/storageAccounts@2024-01-01'>")

      // namespace-qualified resource-derived types
      .matches("type myType = sys.resourceInput<'Microsoft.Storage/storageAccounts@2023-01-01'>.name")
      .matches("type myType = sys.resourceOutput<'Microsoft.Network/virtualNetworks@2023-04-01'>.properties")
      .matches("type myType = sys.resourceInput<'az:Microsoft.Storage/storageAccounts@2022-09-01'>.name")
      .matches("""
        type test = {
          resA: resourceInput<'Microsoft.Storage/storageAccounts@2023-01-01'>.name
          resB: sys.resourceInput<'Microsoft.Storage/storageAccounts@2022-09-01'>.name
          resC: sys.array
          resD: sys.resourceInput<'az:Microsoft.Storage/storageAccounts@2022-09-01'>.name
        }""")

      // comma-separated tuple types
      .matches("type t = [string, int, bool]")
      .matches("type t = [@discriminator('type'), typeA | typeB, string]")
      .matches("type t = [typeA, typeB]")
      .matches("type t = [typeA, typeB,]")
      .matches("type t = [typeA, typeB, @discriminator('type'), typeC]")

      // non-null assertion on parenthesized union types
      .matches("type t = (typeA | typeB)!")
      .matches("type t = (string)!")
      .matches("type t = (int | string | bool)!")
      .matches("type myType = bool[]!")
      .matches("type myType = int?!")
      .matches("type t = (int | string | bool)!")

      .notMatches("type myType")
      .notMatches("type myType=")
      .notMatches("myType=abc")
      .notMatches("type")
      .notMatches("type = abc")
      // From https://github.com/Azure/bicep/commit/7f8aac1852aa85004cc307305acf5fa7d832b2e8
      // However, the `az bicep` returns Error BCP391
      .notMatches("""
        type myObject = {
          quux: int
          saSku: resource<'Microsoft.Storage/storageAccounts@2022-09-01'>.sku
        }""")
      .notMatches("type invalid = resourceInput<'abc', 'def'>")
      .notMatches("type invalid = resourceInput<hello>")
      .notMatches("type invalid = resourceInput<'abc' 'def'>")
      .notMatches("type invalid = resourceInput<123>")
      .notMatches("type invalid = resourceInput<resourceGroup()>")
      .notMatches("type myType = sys.sys.resourceInput<'Microsoft.Storage/storageAccounts@2023-01-01'>.name")
      .notMatches("""
        type myObject = {
          property?: string
        }""")
      // Error BCP391: Type member access is only supported on a reference to a named type.
      .notMatches("type myTypeC = ((stringArrayType?)[*])[]");
  }

  @Test
  void shouldParseSimpleTypeDeclaration() {
    String code = "@description('my type') type myType = abc";
    TypeDeclaration tree = parse(code, BicepLexicalGrammar.TYPE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.TYPE_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("myType");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.type())).containsExactly("abc");
    assertThat(tree.decorators()).hasSize(1);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("@", "description", "(", "my type", ")", "type", "myType", "=", "abc");
  }

  @Test
  void shouldParseResourceDerivedTypeWithoutPropertyAccess() {
    String code = "type myType = resourceOutput<'Microsoft.Network/virtualNetworks@2023-04-01'>";
    TypeDeclaration tree = parse(code, BicepLexicalGrammar.TYPE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.TYPE_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("myType");

    var resourceDerivedType = (ResourceDerivedType) ((SingularTypeExpression) tree.type()).expression();
    assertThat(resourceDerivedType.keyword().value()).isEqualTo("resourceOutput");
    assertThat(resourceDerivedType.typeReference().value()).isEqualTo("Microsoft.Network/virtualNetworks@2023-04-01");
    assertThat(resourceDerivedType.getKind()).isEqualTo(ArmTree.Kind.RESOURCE_DERIVED_TYPE);

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("type", "myType", "=", "resourceOutput", "<", "Microsoft.Network/virtualNetworks@2023-04-01", ">");
  }

  @Test
  void shouldParseNonNullableType() {
    String code = "type nonNullable = nullable!";
    TypeDeclaration tree = parse(code, BicepLexicalGrammar.TYPE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.TYPE_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("nonNullable");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("type", "nonNullable", "=", "nullable", "!");
  }

  @Test
  void shouldParseMemberExpressionType() {
    String code = "type fooProperty = foo.objectProp.intProp";
    TypeDeclaration tree = (TypeDeclaration) createParser(BicepLexicalGrammar.TYPE_DECLARATION).parse(code);
    assertThat(tree.is(ArmTree.Kind.TYPE_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("fooProperty");
  }

  @Test
  void shouldParseNamespaceQualifiedResourceDerivedType() {
    String code = "type myType = sys.resourceInput<'Microsoft.Storage/storageAccounts@2022-09-01'>.name";

    TypeDeclaration tree = (TypeDeclaration) createParser(BicepLexicalGrammar.TYPE_DECLARATION).parse(code);
    assertThat(tree.is(ArmTree.Kind.TYPE_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("myType");

    var compoundType = (CompoundTypeReference) ((SingularTypeExpression) tree.type()).expression();
    assertThat(compoundType.suffix().value()).isEqualTo("name");

    var resourceDerivedType = (ResourceDerivedType) compoundType.baseType();
    assertThat(resourceDerivedType.keyword().value()).isEqualTo("resourceInput");
    assertThat(resourceDerivedType.typeReference().value()).isEqualTo("Microsoft.Storage/storageAccounts@2022-09-01");
    assertThat(resourceDerivedType.getKind()).isEqualTo(ArmTree.Kind.RESOURCE_DERIVED_TYPE);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(resourceDerivedType))
      .containsExactly("sys", ".", "resourceInput", "<", "Microsoft.Storage/storageAccounts@2022-09-01", ">");
  }

  @Test
  void shouldParseNonNullParenthesizedUnionType() {
    String code = "type t = (typeA | typeB)!";
    TypeDeclaration tree = parse(code, BicepLexicalGrammar.TYPE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.TYPE_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("t");

    // The outer type is a SingularTypeExpression with the ! suffix
    var singularType = (SingularTypeExpression) tree.type();
    assertThat(singularType.questionMark()).isNull();
    assertThat(singularType.nonNullAssertion()).isNotNull();
    assertThat(singularType.nonNullAssertion().value()).isEqualTo("!");

    // Inside it is a ParenthesizedTypeExpression wrapping the union
    var parenthesized = (ParenthesizedTypeExpression) singularType.expression();
    assertThat(parenthesized.getKind()).isEqualTo(ArmTree.Kind.PARENTHESIZED_TYPE_EXPRESSION);

    // The union has two members: typeA and typeB
    var unionType = (TypeExpression) parenthesized.typeExpression();
    assertThat(unionType.expressions()).hasSize(2);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(unionType.expressions().get(0)))
      .containsExactly("typeA");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(unionType.expressions().get(1)))
      .containsExactly("typeB");

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("type", "t", "=", "(", "typeA", "|", "typeB", ")", "!");
  }

  @Test
  void shouldParseTupleTypeDeclaration() {
    String code = "type t = [@discriminator('type'), typeA | typeB | { type: 'c', value: object }, string]";
    TypeDeclaration tree = parse(code, BicepLexicalGrammar.TYPE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.TYPE_DECLARATION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("t");

    // The outer type is a SingularTypeExpression wrapping a TupleType
    var singularType = (SingularTypeExpression) tree.type();
    assertThat(singularType.questionMark()).isNull();
    assertThat(singularType.nonNullAssertion()).isNull();

    var tupleType = (org.sonar.iac.arm.tree.api.bicep.TupleType) singularType.expression();
    assertThat(tupleType.getKind()).isEqualTo(ArmTree.Kind.TUPLE_TYPE);
    assertThat(tupleType.items()).hasSize(2);

    // First item has a decorator and a union type expression
    var firstItem = tupleType.items().get(0);
    assertThat(firstItem.decorators()).hasSize(1);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(firstItem.decorators().get(0)))
      .containsExactly("@", "discriminator", "(", "type", ")");

    // Second item is plain string type
    var secondItem = tupleType.items().get(1);
    assertThat(secondItem.decorators()).isEmpty();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(secondItem.typeExpression()))
      .containsExactly("string");

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("type", "t", "=", "[", "@", "discriminator", "(", "type", ")", ",",
        "typeA", "|", "typeB", "|", "{", "type", ":", "c", ",", "value", ":", "object", "}", ",", "string", "]");
  }
}
