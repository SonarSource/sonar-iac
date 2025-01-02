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
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;

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
}
