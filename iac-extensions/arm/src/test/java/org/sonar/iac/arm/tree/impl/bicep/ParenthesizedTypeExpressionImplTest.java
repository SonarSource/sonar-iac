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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedTypeExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ParenthesizedTypeExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseParenthesizedTypeExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.PARENTHESIZED_TYPE_EXPRESSION)
      // ambient Type Reference
      .matches("(array)")
      .matches("( array )")
      .matches("( array)")
      .matches("(   array )")
      .matches("( array[] )")
      .matches("( array? )")
      .matches("( bool | int )")
      .matches("( bool[] | int? )")
      .matches("( bool[]?[] | int?? )")
      // identifier
      .matches("( abc )")
      .matches("( aa222bbb )")
      .matches("( _A1 )")
      // literal value: Numeric Literal
      .matches("( 5 )")
      .matches("( 0 )")
      .matches("( 123456 )")
      // literal value: Boolean Literal
      .matches("( true )")
      .matches("( false )")
      // literal value: null
      .matches("( null )")
      // unaryOperatorLiteralValue
      .matches("( !5 )")
      .matches("( ! 5 )")
      .matches("( -5 )")
      .matches("( +5 )")
      .matches("( +true )")
      .matches("( + true )")
      .matches("( +false )")
      .matches("( +null )")
      // string complete
      .matches("( '123' )")
      .matches("( 'abc' )")
      .matches("( 'a$b' )")
      .matches("( 'a{}b' )")
      // multiline string
      .matches("( '''''' )")
      .matches("( '''python main.py''' )")
      .matches("( '''python main.py --abc ${{input.abc}} --def ${xyz}''' )")
      .matches(code("( '''",
        "first line",
        "second line",
        "''' )"))
      .matches(code("( '''",
        "first line",
        "// inline comment",
        "''' )"))
      .matches(code("( '''",
        "first line",
        "/* inline comment */",
        "''' )"))
      .matches(code("( '''",
        "it''s awesome",
        "''' )"))
      // object type
      .matches("( {} )")
      .matches("( { } )")
      .matches("( {\n} )")
      .matches("( { identifier : abc } )")
      .matches("( {\n'string complete' : abc\n} )")
      .matches("( {\n'''\nsingle\nmultiline\n''' : abc\n} )")
      .matches("( {*: abc} )")
      .matches("( {\n*: abc\n} )")
      // tuple type
      .matches("( [] )")
      .matches("( [ ] )")
      .matches("( [\n] )")
      .matches("( [\ntypeExpr\n] )")
      .matches("( [\ntypeExpr\ntypeN\n] )")
      .matches("( [\n@description('some desc')\n@maxLength(12)\ntypeExpr\n] )")

      .notMatches("()")
      .notMatches("(array")
      .notMatches("( array")
      .notMatches("array)")
      .notMatches("array )");
  }

  @Test
  void shouldParseSimpleParenthesizedTypeExpression() {
    ParenthesizedTypeExpression tree = parse("( array )", BicepLexicalGrammar.PARENTHESIZED_TYPE_EXPRESSION);
    assertThat(tree.is(ArmTree.Kind.PARENTHESIZED_TYPE_EXPRESSION)).isTrue();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("(", "array", ")");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.typeExpression())).containsExactly("array");
  }
}
