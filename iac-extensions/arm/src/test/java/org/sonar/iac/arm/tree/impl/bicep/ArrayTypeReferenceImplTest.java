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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.ArrayTypeReference;

import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class ArrayTypeReferenceImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseArrayTypeReference() {
    assertThat(BicepLexicalGrammar.ARRAY_TYPE_REFERENCE)
      .matches("array[]")
      .matches("bool[]")
      .matches("int[]")
      .matches("object[]")
      .matches("string[]")
      .matches("  string[]")
      .matches("abc[]")
      .matches("string[0]")
      .matches("string[3]")
      .matches("string[][]")
      .matches("string[][3]")
      .matches("string[3][]")
      .matches("string[3][7]")
      .matches("string[3][7][10]")

      .notMatches("string")
      .notMatches("typeExpr")
      .notMatches("[]typeExpr")
      .notMatches("[\ntypeExpr")
      .notMatches("typeExpr[")
      .notMatches("typeExpr]")
      .notMatches("typeExpr[][")
      .notMatches("typeExpr[]]")
      .notMatches("typeExpr[[]]")
      .notMatches("[]");
  }

  @Test
  void shouldParseAmbientArrayTypeReference() {
    ArrayTypeReference tree = parse("string[]", BicepLexicalGrammar.ARRAY_TYPE_REFERENCE);
    Assertions.assertThat(tree.value()).isEqualTo("string[]");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("string[]");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 8);
  }

  @Test
  void shouldParseIdentifierArrayTypeReference() {
    ArrayTypeReference tree = parse("abc[]", BicepLexicalGrammar.ARRAY_TYPE_REFERENCE);
    Assertions.assertThat(tree.value()).isEqualTo("abc[]");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("abc[]");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 5);
  }

  @Test
  void shouldParseMultiDimensionalArrayTypeReference() {
    ArrayTypeReference tree = parse("string[][][]", BicepLexicalGrammar.ARRAY_TYPE_REFERENCE);
    Assertions.assertThat(tree.value()).isEqualTo("string[][][]");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("string[][][]");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 12);
  }

  @Test
  void shouldParseArrayTypeReferenceWithSize() {
    ArrayTypeReference tree = parse("string[3][]", BicepLexicalGrammar.ARRAY_TYPE_REFERENCE);
    Assertions.assertThat(tree.value()).isEqualTo("string[3][]");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("string[3][]");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 11);
  }
}
