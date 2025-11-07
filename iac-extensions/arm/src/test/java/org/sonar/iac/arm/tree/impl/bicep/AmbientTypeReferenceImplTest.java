/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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

import com.sonar.sslr.api.RecognitionException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class AmbientTypeReferenceImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseAmbientTypeReference() {
    assertThat(BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE)
      .matches("array")
      .matches("  array")
      .matches("bool")
      .matches("  bool")
      .matches("int")
      .matches("  int")
      .matches("object")
      .matches("  object")
      .matches("string")
      .matches("  string")

      .notMatches("String")
      .notMatches("stringeee")
      .notMatches("arrayy")
      .notMatches("STRING")
      .notMatches("int1")
      .notMatches("intelligent")
      .notMatches("int_eger")
      .notMatches("int$")
      .notMatches("int_")
      .notMatches("int1")
      .notMatches("intEGER")
      .notMatches("objective")
      .notMatches("1string")
      .notMatches("barray")
      .notMatches("foo")
      .notMatches("123")
      .notMatches("variable a = 10")
      .notMatches("{}")
      .notMatches("-10");
  }

  @Test
  void shouldParseSimpleArrayTypeReference() {
    AmbientTypeReference tree = parse("array", BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(tree.value()).isEqualTo("array");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("array");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 5);
  }

  @Test
  void shouldParseSimpleBooleanTypeReference() {
    AmbientTypeReference tree = parse("bool", BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(tree.value()).isEqualTo("bool");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("bool");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 4);
  }

  @Test
  void shouldParseSimpleIntegerTypeReference() {
    AmbientTypeReference tree = parse("int", BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(tree.value()).isEqualTo("int");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("int");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 3);
  }

  @Test
  void shouldParseSimpleObjectTypeReference() {
    AmbientTypeReference tree = parse("object", BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(tree.value()).isEqualTo("object");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("object");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 6);
  }

  @Test
  void shouldParseSimpleStringTypeReference() {
    AmbientTypeReference tree = parse("string", BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(tree.value()).isEqualTo("string");
    Assertions.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("string");
    ArmAssertions.assertThat(tree.textRange()).hasRange(1, 0, 1, 6);
  }

  @Test
  void shouldNotParseBiggerWord() {
    assertThatThrownBy(() -> parse("intelligent", BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE))
      .isInstanceOf(RecognitionException.class)
      .hasMessageStartingWith("Parse error at line 1 column 1");
  }
}
