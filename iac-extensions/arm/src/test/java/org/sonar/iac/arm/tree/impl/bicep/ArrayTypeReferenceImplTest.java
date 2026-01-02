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
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.ArrayTypeReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class ArrayTypeReferenceImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseArrayType() {
    String code = "string[3]";
    ArrayTypeReference tree = parse(code, BicepLexicalGrammar.TYPE_REFERENCE);
    assertThat(tree.is(ArmTree.Kind.ARRAY_TYPE_REFERENCE)).isTrue();
    assertThat(tree.getType().is(ArmTree.Kind.AMBIENT_TYPE_REFERENCE)).isTrue();
    assertThat(((AmbientTypeReference) tree.getType()).value()).isEqualTo("string");
    assertThat(tree.getIndex()).isNotNull();
    assertThat(tree.getIndex().is(ArmTree.Kind.NUMERIC_LITERAL)).isTrue();
    assertThat(tree.getIndex().value()).isEqualTo("3");
    assertThat(tree.getStar()).isNull();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("string", "[", "3", "]");
  }

  @Test
  void shouldParseArrayTypeWithoutLength() {
    String code = "bool[]";
    ArrayTypeReference tree = parse(code, BicepLexicalGrammar.TYPE_REFERENCE);
    assertThat(tree.is(ArmTree.Kind.ARRAY_TYPE_REFERENCE)).isTrue();
    assertThat(tree.getType().is(ArmTree.Kind.AMBIENT_TYPE_REFERENCE)).isTrue();
    assertThat(((AmbientTypeReference) tree.getType()).value()).isEqualTo("bool");
    assertThat(tree.getIndex()).isNull();
    assertThat(tree.getStar()).isNull();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("bool", "[", "]");
  }

  @Test
  void shouldParseMultiDimensionalArrayType() {
    String code = "int[3][5][7]";
    ArrayTypeReference tree = parse(code, BicepLexicalGrammar.TYPE_REFERENCE);

    assertThat(tree.is(ArmTree.Kind.ARRAY_TYPE_REFERENCE)).isTrue();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("int", "[", "3", "]", "[", "5", "]", "[", "7", "]");

    ArrayTypeReference innerType1 = (ArrayTypeReference) tree.getType();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(innerType1))
      .containsExactly("int", "[", "3", "]", "[", "5", "]");

    ArrayTypeReference innerType2 = (ArrayTypeReference) innerType1.getType();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(innerType2))
      .containsExactly("int", "[", "3", "]");

    AmbientTypeReference innerType3 = (AmbientTypeReference) innerType2.getType();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(innerType3))
      .containsExactly("int");
  }

  @Test
  void shouldParseMultiDimensionalArrayTypeWithMixedLength() {
    String code = "object[][15][0]";
    ArrayTypeReference tree = parse(code, BicepLexicalGrammar.TYPE_REFERENCE);
    assertThat(tree.is(ArmTree.Kind.ARRAY_TYPE_REFERENCE)).isTrue();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("object", "[", "]", "[", "15", "]", "[", "0", "]");
  }

  @Test
  void shouldParseArrayTypeWithIdentifier() {
    String code = "typeIdentifier[3]";
    ArrayTypeReference tree = parse(code, BicepLexicalGrammar.TYPE_REFERENCE);
    assertThat(tree.is(ArmTree.Kind.ARRAY_TYPE_REFERENCE)).isTrue();
    assertThat(tree.getType().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    assertThat(((Identifier) tree.getType()).value()).isEqualTo("typeIdentifier");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("typeIdentifier", "[", "3", "]");
  }

  @Test
  void shouldParseArrayStarType() {
    String code = "string[*]";
    ArrayTypeReference tree = parse(code, BicepLexicalGrammar.TYPE_REFERENCE);
    assertThat(tree.is(ArmTree.Kind.ARRAY_TYPE_REFERENCE)).isTrue();
    assertThat(tree.getType().is(ArmTree.Kind.AMBIENT_TYPE_REFERENCE)).isTrue();
    assertThat(((AmbientTypeReference) tree.getType()).value()).isEqualTo("string");
    assertThat(tree.getIndex()).isNull();
    assertThat(tree.getStar()).isNotNull();
    assertThat(tree.getStar().value()).isEqualTo("*");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("string", "[", "*", "]");
  }

  @Test
  void shouldConvertToString() {
    ArrayTypeReference tree = parse("string[5][15][]", BicepLexicalGrammar.TYPE_REFERENCE);
    assertThat(tree).hasToString("string[5][15][]");
  }
}
