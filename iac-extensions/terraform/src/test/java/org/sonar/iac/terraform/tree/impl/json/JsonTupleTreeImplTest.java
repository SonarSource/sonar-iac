/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.tree.impl.json;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.tree.impl.SeparatedTreesImpl;

import static org.assertj.core.api.Assertions.assertThat;

class JsonTupleTreeImplTest {

  private static final TextRange RANGE = new TextRange(new TextPointer(1, 0), new TextPointer(3, 8));

  @Test
  void shouldExposeElementsAndRange() {
    JsonLiteralExprTreeImpl first = literal("a");
    SeparatedTrees<ExpressionTree> elements = new SeparatedTreesImpl<>(List.of(first), List.of());
    SyntaxToken open = new JsonSyntaxTokenImpl("[", RANGE);
    SyntaxToken close = new JsonSyntaxTokenImpl("]", RANGE);

    JsonTupleTreeImpl tuple = new JsonTupleTreeImpl(elements, open, close, RANGE);

    assertThat(tuple.elements()).isSameAs(elements);
    assertThat(tuple.textRange()).isSameAs(RANGE);
  }

  @Test
  void shouldIterateOverElementsInOrder() {
    JsonLiteralExprTreeImpl first = literal("a");
    JsonLiteralExprTreeImpl second = literal("b");
    SyntaxToken separator = new JsonSyntaxTokenImpl(",", RANGE);
    SeparatedTrees<ExpressionTree> elements = new SeparatedTreesImpl<>(List.of(first, second), List.of(separator));

    JsonTupleTreeImpl tuple = new JsonTupleTreeImpl(elements, new JsonSyntaxTokenImpl("[", RANGE), new JsonSyntaxTokenImpl("]", RANGE), RANGE);

    assertThat(tuple).containsExactly(first, second);
  }

  @Test
  void shouldExposeChildrenAsBracketsWithElementsAndSeparators() {
    JsonLiteralExprTreeImpl first = literal("a");
    JsonLiteralExprTreeImpl second = literal("b");
    SyntaxToken separator = new JsonSyntaxTokenImpl(",", RANGE);
    SeparatedTrees<ExpressionTree> elements = new SeparatedTreesImpl<>(List.of(first, second), List.of(separator));
    SyntaxToken open = new JsonSyntaxTokenImpl("[", RANGE);
    SyntaxToken close = new JsonSyntaxTokenImpl("]", RANGE);

    JsonTupleTreeImpl tuple = new JsonTupleTreeImpl(elements, open, close, RANGE);

    assertThat(tuple.children()).containsExactly(open, first, separator, second, close);
  }

  @Test
  void shouldExposeEmptyChildrenBetweenBracketsForEmptyArray() {
    SeparatedTrees<ExpressionTree> elements = new SeparatedTreesImpl<>(List.of(), List.of());
    SyntaxToken open = new JsonSyntaxTokenImpl("[", RANGE);
    SyntaxToken close = new JsonSyntaxTokenImpl("]", RANGE);

    JsonTupleTreeImpl tuple = new JsonTupleTreeImpl(elements, open, close, RANGE);

    assertThat(tuple).isEmpty();
    assertThat(tuple.children()).containsExactly(open, close);
  }

  @Test
  void shouldReportTupleKind() {
    JsonTupleTreeImpl tuple = emptyTuple();

    assertThat(tuple.getKind()).isEqualTo(Kind.TUPLE);
    assertThat(tuple.is(Kind.TUPLE)).isTrue();
  }

  @Test
  void shouldMatchTupleAmongMultipleKinds() {
    JsonTupleTreeImpl tuple = emptyTuple();

    assertThat(tuple.is(Kind.OBJECT, Kind.TUPLE, Kind.STRING_LITERAL)).isTrue();
  }

  @Test
  void shouldNotMatchUnrelatedKinds() {
    JsonTupleTreeImpl tuple = emptyTuple();

    assertThat(tuple.is(Kind.OBJECT)).isFalse();
    assertThat(tuple.is(Kind.OBJECT, Kind.STRING_LITERAL)).isFalse();
    assertThat(tuple.is()).isFalse();
  }

  private static JsonTupleTreeImpl emptyTuple() {
    SeparatedTrees<ExpressionTree> elements = new SeparatedTreesImpl<>(List.of(), List.of());
    return new JsonTupleTreeImpl(elements, new JsonSyntaxTokenImpl("[", RANGE), new JsonSyntaxTokenImpl("]", RANGE), RANGE);
  }

  private static JsonLiteralExprTreeImpl literal(String value) {
    return new JsonLiteralExprTreeImpl(Kind.STRING_LITERAL, value, new JsonSyntaxTokenImpl(value, RANGE), RANGE);
  }
}
