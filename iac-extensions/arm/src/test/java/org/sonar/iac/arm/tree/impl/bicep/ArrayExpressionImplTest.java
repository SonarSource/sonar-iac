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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SpreadExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.EqualityExpression;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class ArrayExpressionImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseArrayExpressions() {
    ArmAssertions.assertThat(BicepLexicalGrammar.ARRAY_EXPRESSION)
      .matches("""
        [
        ]""")
      .matches("""
        [
        'a'
        ]""")
      .matches("['a', 'b']")
      .matches("['a', 'b',]")
      .matches("['a', 'b', ...identifier]")
      .matches("['a', 'b', ...identifier,]")
      .matches("""
        [
        'a', 'b'
        'c'
        ]""")
      .matches("""
        [
        'a'
        'b'
        ]""")
      .matches("""
        [
            'a'
            'b'
        ]""")
      .matches("""
        [


           'a'

            'b'

          ...identifier
        ]""")
      .matches("[]")
      .matches("""
        ['a'
        ]""")
      .matches("['a']")
      .matches("""
        [
        'a']""")

      .notMatches("[,'a', 'b']")
      .notMatches("['a', 'b', ..identifier]")
      .notMatches("['a', 'b', ....identifier]")
      .notMatches("['a', 'b', identifier...]")
      .notMatches("['a', 'b', ...identifier...]")
      .notMatches("""
        [
        }""");
  }

  @Test
  void shouldParseValidExpression() {
    ArrayExpression tree = parse("""
      [
      'a'
      'b'
      ]""", BicepLexicalGrammar.ARRAY_EXPRESSION);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(ArrayExpression.class);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_EXPRESSION);
    softly.assertThat(tree.elements()).hasSize(2);
    softly.assertAll();
  }

  @Test
  void shouldParseMixedInlineAndMultilineArray() {
    String code = """
      [
      'a', 'b'
      'c'
      ]""";
    ArrayExpression tree = parse(code, BicepLexicalGrammar.ARRAY_EXPRESSION);

    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_EXPRESSION);
    assertThat(tree.elements()).hasSize(3);
    StringLiteral a = (StringLiteral) tree.elements().get(0);
    StringLiteral b = (StringLiteral) tree.elements().get(1);
    StringLiteral c = (StringLiteral) tree.elements().get(2);
    ArmAssertions.assertThat(a).hasValue("a");
    ArmAssertions.assertThat(b).hasValue("b");
    ArmAssertions.assertThat(c).hasValue("c");

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("[", "a", ",", "b", "c", "]");
  }

  @Test
  void shouldParseArrayWithBinaryExpression() {
    String code = """
      [
      'a'
      'a' =~ 'b'
      ]""";
    ArrayExpression tree = parse(code, BicepLexicalGrammar.ARRAY_EXPRESSION);

    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_EXPRESSION);
    assertThat(tree.elements()).hasSize(2);
    StringLiteral a = (StringLiteral) tree.elements().get(0);
    EqualityExpression equalityExpression = (EqualityExpression) tree.elements().get(1);
    ArmAssertions.assertThat(a).hasValue("a");
    assertThat(equalityExpression.getKind()).isEqualTo(ArmTree.Kind.EQUALITY_EXPRESSION);

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("[", "a", "a", "=~", "b", "]");
  }

  @Test
  void shouldParseArrayWithSpreadExpression() {
    String code = """
      [
      'a'
      ...identifier
      ]""";
    ArrayExpression tree = parse(code, BicepLexicalGrammar.ARRAY_EXPRESSION);

    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_EXPRESSION);
    assertThat(tree.elements()).hasSize(2);
    StringLiteral a = (StringLiteral) tree.elements().get(0);
    SpreadExpression spreadExpression = (SpreadExpression) tree.elements().get(1);
    ArmAssertions.assertThat(a).hasValue("a");
    assertThat(spreadExpression.getKind()).isEqualTo(ArmTree.Kind.SPREAD_EXPRESSION);

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("[", "a", "...", "identifier", "]");
  }
}
