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
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class SingularTypeExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseUnaryOperatorLiteralValue() {
    ArmAssertions.assertThat(BicepLexicalGrammar.SINGULAR_TYPE_EXPRESSION)
      // ambient Type Reference
      .matches("array")
      .matches("  array")
      .matches("array[]")
      .matches("array?")
      .matches("array[][]")
      .matches("array[][][]")
      .matches("array[]?")
      .matches("array[][]?")
      .matches("bool")
      .matches("int")
      // identifier
      .matches("abc")
      .matches("A")
      .matches("Z")
      .matches("a")
      .matches("z")
      .matches("AAAAA123")
      .matches("aa222bbb")
      .matches("_A1")
      // literal value: Numeric Literal
      .matches("5")
      .matches("0")
      .matches("123456")
      // literal value: Boolean Literal
      .matches("true")
      .matches("false")
      // literal value: null
      .matches("null")
      // unaryOperatorLiteralValue
      .matches("!5")
      .matches("! 5")
      .matches("-5")
      .matches("+5")
      .matches("+true")
      .matches("+ true")
      .matches("+false")
      .matches("+null")
      // string complete
      .matches("'123'")
      .matches("'abc'")
      .matches("  'abc'")
      .matches("'A'")
      .matches("'Z'")
      .matches("'a'")
      .matches("'z'")
      .matches("'AAAAA123'")
      .matches("'123zz'")
      .matches("'123aa789'")
      .matches("'123BB789'")
      .matches("'a$b'")
      .matches("'a{}b'")
      // multiline string
      .matches("''''''")
      .matches("'''python main.py'''")
      .matches("'''python main.py --abc ${{input.abc}} --def ${xyz}'''")
      .matches("""
        '''
        first line
        second line
        '''""")
      .matches("""
        '''
        first line
        // inline comment
        '''""")
      .matches("""
        '''
        first line
        /* inline comment */
        '''""")
      .matches("""
        '''
        first line
        /* inline
        comment */
        '''""")
      .matches("""
        '''
        it's awesome
        '''""")
      .matches("""
        '''
        it''s awesome
        '''""")
      // object type
      .matches("{}")
      .matches("{ }")
      .matches("{\n}")
      .matches("{ identifier : abc }")
      .matches("{\nidentifier : abc\n}")
      .matches("{\n'string complete' : abc\n}")
      .matches("{\n'''single multiline''' : abc\n}")
      .matches("{\n'''\nsingle\nmultiline\n''' : abc\n}")
      .matches("{*: abc}")
      .matches("{\n*: abc\n}")
      // tuple type
      .matches("[]")
      .matches("[ ]")
      .matches("[\n]")
      .matches("[\ntypeExpr\n]")
      .matches("[\ntypeExpr\ntypeN\n]")
      .matches("[\n@functionName123() typeExpr\n]")
      .matches("[@description('some desc')\n@maxLength(12)\ntypeExpr\n]")
      .matches("[\n@description('some desc')\n@maxLength(12)\ntypeExpr\n]")
      // parenthesizedTypeExpression
      .matches("( array )")
      .matches("( abc )")
      .matches("( 5 )")
      .matches("( true )")
      .matches("( false )")
      .matches("( null )")
      .matches("( !5 )")
      .matches("( +true )")
      .matches("( -null )")
      .matches("( '123' )")
      .matches("( '''python main.py''' )")
      .matches("( { } )")
      .matches("( [\ntypeExpr\n] )")

      .notMatches("(array")
      .notMatches("( array")
      .notMatches("array)")
      .notMatches("array )")
      .notMatches("array ]")
      .notMatches("[ array")
      .notMatches("array[]?[]")
      .notMatches("array??");
  }

  @Test
  void shouldParseComplexSingularTypeExpression() {
    var tree = (SingularTypeExpression) parse("( abc )?", BicepLexicalGrammar.SINGULAR_TYPE_EXPRESSION);
    assertThat(tree.is(ArmTree.Kind.SINGULAR_TYPE_EXPRESSION)).isTrue();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("(", "abc", ")", "?");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.expression()))
      .containsExactly("(", "abc", ")");
    assertThat(tree.questionMark().value()).isEqualTo("?");
  }
}
