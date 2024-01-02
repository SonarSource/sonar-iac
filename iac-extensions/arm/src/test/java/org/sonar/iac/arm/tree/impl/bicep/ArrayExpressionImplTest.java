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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ArrayExpressionImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseMultilineArrays() {
    ArmAssertions.assertThat(BicepLexicalGrammar.ARRAY_EXPRESSION)
      .matches(code("[", "]"))
      .matches(code("[", "'a'", "]"))
      .matches(code("['a', 'b']"))
      .matches(code("[", "'a', 'b'", "'c'", "]"))
      .matches(code("[", "'a'", "'b'", "]"))
      .matches(code("[", "    'a'", "    'b'", "]"))
      .matches(code("[", "", "", "   'a'", "", "    'b'", "]"))
      .matches(code("[]"))
      .matches(code("['a'", "]"))
      .matches(code("['a']"))
      .matches(code("[", "'a']"))

      .notMatches(code("[", "}"));
  }

  @Test
  void shouldParseValidExpression() {
    ArrayExpression tree = parse(code("[", "'a'", "'b'", "]"), BicepLexicalGrammar.ARRAY_EXPRESSION);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(ArrayExpression.class);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_EXPRESSION);
    softly.assertThat(tree.elements()).hasSize(2);
    softly.assertAll();
  }

  @Test
  void shouldParseMixedInlineAndMultilineArray() {
    String code = code("[",
      "'a', 'b'",
      "'c'",
      "]");
    ArrayExpression tree = parse(code, BicepLexicalGrammar.ARRAY_EXPRESSION);

    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ARRAY_EXPRESSION);
    assertThat(tree.elements()).hasSize(3);
    StringComplete a = (StringComplete) tree.elements().get(0);
    StringComplete b = (StringComplete) tree.elements().get(1);
    StringComplete c = (StringComplete) tree.elements().get(2);
    ArmAssertions.assertThat(a.content()).hasValue("a");
    ArmAssertions.assertThat(b.content()).hasValue("b");
    ArmAssertions.assertThat(c.content()).hasValue("c");

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("[", "a", ",", "b", "c", "]");
  }
}
