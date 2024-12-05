/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.TupleType;
import org.sonar.iac.arm.tree.api.bicep.TypeExpression;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class TypeExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseTypeExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.TYPE_EXPRESSION)
      // ambient Type Reference
      .matches("array")
      .matches("  array")
      .matches("array[]")
      .matches("array?")
      .matches("array[][]")
      .matches("array[][][]")
      .matches("array[]?")
      .matches("array?")
      .matches("bool")
      .matches("int")
      .matches("array | int")
      .matches("['foo'] | ['bar'] | ['bazz']")
      .matches("['foo']")
      .matches("| ['foo']")
      .matches("| value")
      .matches("| value1 | value2")
      .matches("| value1\n| value2")
      .matches("['foo', 'bar', 'baz'] | ['fizz', 'buzz'] | ['snap', 'crackle', 'pop']")
      .matches("bool | int")
      .matches("bool[] | int?")
      .matches("tuple[1].name")
      .matches("tuple[*]")
      .matches("tuple[*].name")
      .matches("withNested.*.foo")
      .matches("very.long[1].type[]")
      .matches("very.long[1].type[0]")
      .matches("very.long[1].type.*")
      .matches("very.long[1].type.*.foo")
      .matches("bool[][]? | int")
      .matches("bool[][] | int?")

      .notMatches("array[]?[]")
      .notMatches("array??")
      .notMatches("bool[]?[]")
      .notMatches("int??")
      .notMatches("|")
      .notMatches("values |")
      .notMatches("| | values");
  }

  @Test
  void shouldParseSimpleTypeExpression() {
    SingularTypeExpression tree = parse("array", BicepLexicalGrammar.TYPE_EXPRESSION);
    assertThat(tree).is(ArmTree.Kind.SINGULAR_TYPE_EXPRESSION);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("array");
  }

  @Test
  void shouldParseComplexTypeExpression() {
    TypeExpression tree = parse("array | ( abc )? | null ", BicepLexicalGrammar.TYPE_EXPRESSION);
    assertThat(tree).is(ArmTree.Kind.TYPE_EXPRESSION);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("array", "|", "(", "abc", ")", "?", "|", "null");
    List<SingularTypeExpression> expressions = tree.expressions();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(expressions.get(0)))
      .containsExactly("array");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(expressions.get(1)))
      .containsExactly("(", "abc", ")", "?");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(expressions.get(2)))
      .containsExactly("null");
    assertThat(expressions).hasSize(3);
  }

  @Test
  void shouldParseTypeExpressionUnionArray() {
    TypeExpression tree = parse("['foo'] | ['bar'] | ['bazz']", BicepLexicalGrammar.TYPE_EXPRESSION);
    assertThat(tree).is(ArmTree.Kind.TYPE_EXPRESSION);
    assertThat(tree.expressions()).hasSize(3);

    var expr1 = tree.expressions().get(0);
    assertThat(expr1.getKind()).isEqualTo(ArmTree.Kind.SINGULAR_TYPE_EXPRESSION);
    assertArraySingleStringValue(expr1.expression(), "foo");
    var expr2 = tree.expressions().get(1);
    assertThat(expr2.getKind()).isEqualTo(ArmTree.Kind.SINGULAR_TYPE_EXPRESSION);
    assertArraySingleStringValue(expr2.expression(), "bar");
    var expr3 = tree.expressions().get(2);
    assertThat(expr3.getKind()).isEqualTo(ArmTree.Kind.SINGULAR_TYPE_EXPRESSION);
    assertArraySingleStringValue(expr3.expression(), "bazz");
  }

  void assertArraySingleStringValue(TypeExpressionAble expr, String value) {
    assertThat(expr.getKind()).isEqualTo(ArmTree.Kind.ARRAY_EXPRESSION);
    var array = (ArrayExpression) expr;
    assertThat(array.elements()).hasSize(1);
    var element = array.elements().get(0);
    assertThat(element.is(ArmTree.Kind.STRING_LITERAL)).isTrue();
    assertThat(((StringLiteral) element).value()).isEqualTo(value);
  }

  @Test
  void shouldParseTypeExpressionTupleType() {
    SingularTypeExpression tree = parse("[\n@functionName123() typeExpr\n]", BicepLexicalGrammar.TYPE_EXPRESSION);
    var expr = tree.expression();
    assertThat(expr).is(ArmTree.Kind.TUPLE_TYPE);
    var tuple = (TupleType) expr;
    assertThat(tuple.items()).hasSize(1);
    var item = tuple.items().get(0);
    assertThat(item.decorators()).hasSize(1);
    assertThat(item.typeExpression()).is(ArmTree.Kind.SINGULAR_TYPE_EXPRESSION);
    var itemTypeExpression = (SingularTypeExpression) item.typeExpression();
    assertThat(itemTypeExpression.expression()).is(ArmTree.Kind.IDENTIFIER);
    assertThat(itemTypeExpression.questionMark()).isNull();
    var identifier = (Identifier) itemTypeExpression.expression();
    assertThat(identifier.value()).isEqualTo("typeExpr");
  }
}
