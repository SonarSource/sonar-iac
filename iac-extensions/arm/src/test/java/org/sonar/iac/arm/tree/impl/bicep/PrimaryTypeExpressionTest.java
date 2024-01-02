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
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NullLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.MultilineString;
import org.sonar.iac.arm.tree.api.bicep.ObjectType;
import org.sonar.iac.arm.tree.api.bicep.ObjectTypeProperty;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;
import org.sonar.iac.arm.tree.api.bicep.TupleType;
import org.sonar.iac.arm.tree.api.bicep.expression.UnaryExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class PrimaryTypeExpressionTest extends BicepTreeModelTest {
  @Test
  void shouldParseUnaryExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION)
      // ambient Type Reference
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
      // UnaryExpression
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
      .matches(code("'''",
        "first line",
        "second line",
        "'''"))
      .matches(code("'''",
        "first line",
        "// inline comment",
        "'''"))
      .matches(code("'''",
        "first line",
        "/* inline comment */",
        "'''"))
      .matches(code("'''",
        "first line",
        "/* inline",
        "comment */",
        "'''"))
      .matches(code("'''",
        "it's awesome",
        "'''"))
      .matches(code("'''",
        "it''s awesome",
        "'''"))
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
      .matches("[\ntypeExpr\ntypeExpr2]")

      .notMatches("!!")
      .notMatches("!!12")
      .notMatches("-trueeee")
      .notMatches("-+ false")
      .notMatches("1string")
      .notMatches("var a = 10")
      .notMatches("variable a = 10")
      .notMatches("-")
      .notMatches("$123")
      .notMatches("{123}")
      .notMatches("(abc")
      .notMatches("!!12")
      .notMatches("-trueeee")
      .notMatches("-+ false")
      .notMatches("+ tru")
      .notMatches("5 +")
      .notMatches("! nulllll")
      .notMatches("-5.5")
      .notMatches("+ f")
      .notMatches("''''")
      .notMatches("'''ab''''")
      .notMatches("''ab''''")
      .notMatches("''ab''")
      .notMatches("'''ab'")
      .notMatches("'''ab''")
      .notMatches("identifier :")
      .notMatches("output myOutput : abc")
      .notMatches("foo bar : baz")
      .notMatches("foo 'bar' : baz")
      .notMatches("foo '''bar''' : baz")
      .notMatches("identifier = abc")
      .notMatches("identifier = {}")
      .notMatches("[]typeExpr")
      .notMatches("[\ntypeExpr")
      .notMatches("typeExpr]")
      .notMatches("{typeExpr}");
  }

  @Test
  void shouldParseSimpleUnaryExpression() {
    UnaryExpression tree = parse("- 5", BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION);
    assertThat(tree.is(ArmTree.Kind.UNARY_EXPRESSION)).isTrue();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("-", "5");
  }

  @Test
  void shouldParseSimpleArrayTypeReference() {
    AmbientTypeReference tree = parse("array", BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION);
    Assertions.assertThat(tree.value()).isEqualTo("array");
    ArmAssertions.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.AMBIENT_TYPE_REFERENCE);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("array");
    ArmAssertions.assertThat(tree.textRange()).hasRange(1, 0, 1, 5);
  }

  @Test
  void shouldParseSimpleIdentifier() {
    Identifier tree = parse("abc123DEF", BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION);
    assertThat(tree.value()).isEqualTo("abc123DEF");
    assertThat(tree.is(ArmTree.Kind.IDENTIFIER)).isTrue();
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("abc123DEF");
  }

  @Test
  void shouldParseNullValue() {
    NullLiteral tree = parse("null", BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION);
    ArmAssertions.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.NULL_LITERAL);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("null");
  }

  @Test
  void shouldParseTrueValue() {
    BooleanLiteral tree = parse("true", BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION);
    ArmAssertions.assertThat(tree).isTrue();
    ArmAssertions.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.BOOLEAN_LITERAL);
  }

  @Test
  void shouldParseNumericValue() {
    NumericLiteral tree = parse("123", BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION);
    ArmAssertions.assertThat(tree).hasValue(123);
    ArmAssertions.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.NUMERIC_LITERAL);
  }

  @Test
  void shouldParseSimpleStringComplete() {
    StringComplete tree = parse("'abc123DEF'", BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION);
    assertThat(tree.value()).isEqualTo("abc123DEF");
    assertThat(tree.is(ArmTree.Kind.STRING_COMPLETE)).isTrue();
  }

  @Test
  void shouldParseSimpleMultilineString() {
    String code = code("'''",
      "a",
      "123",
      "BBB",
      "'''");

    MultilineString tree = parse(code, BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION);

    Assertions.assertThat(tree.value()).isEqualTo("a\n123\nBBB\n");
    ArmAssertions.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.MULTILINE_STRING);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("'''", "a\n123\nBBB\n", "'''");
    ArmAssertions.assertThat(tree.textRange()).hasRange(1, 0, 5, 3);
  }

  @Test
  void shouldParseSimpleObjectType() {
    ObjectType tree = parse("{ identifier : abc }", BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION);

    assertThat(tree.is(ArmTree.Kind.OBJECT_TYPE)).isTrue();
    ObjectTypeProperty property = (ObjectTypeProperty) tree.properties().get(0);
    assertThat(property.name()).isInstanceOf(Identifier.class);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(property.typeExpression()))
      .containsExactly("abc");
  }

  @Test
  void shouldParseSimpleTupleType() {
    String code = code("[",
      "@functionName123() typeExpr",
      "]");
    TupleType tree = parse(code, BicepLexicalGrammar.TUPLE_TYPE);
    assertThat(tree.is(ArmTree.Kind.TUPLE_TYPE)).isTrue();
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("[", "@", "functionName123", "(", ")", "typeExpr", "]");
  }
}
