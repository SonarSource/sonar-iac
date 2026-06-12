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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class JsonObjectElementTreeImplTest {

  private static final TextRange RANGE = new TextRange(new TextPointer(1, 0), new TextPointer(2, 10));

  @Test
  void shouldExposeKeyValueSeparatorAndRange() {
    ExpressionTree key = literal("name");
    SyntaxToken colon = new JsonSyntaxTokenImpl(":", RANGE);
    ExpressionTree value = literal("alice");

    JsonObjectElementTreeImpl element = new JsonObjectElementTreeImpl(key, colon, value, RANGE);

    assertThat(element.key()).isSameAs(key);
    assertThat(element.equalOrColonSign()).isSameAs(colon);
    assertThat(element.value()).isSameAs(value);
    assertThat(element.textRange()).isSameAs(RANGE);
  }

  @Test
  void shouldExposeChildrenInKeySeparatorValueOrder() {
    ExpressionTree key = literal("k");
    SyntaxToken colon = new JsonSyntaxTokenImpl(":", RANGE);
    ExpressionTree value = literal("v");

    JsonObjectElementTreeImpl element = new JsonObjectElementTreeImpl(key, colon, value, RANGE);

    assertThat(element.children()).containsExactly(key, colon, value);
  }

  @Test
  void shouldReportObjectElementKind() {
    JsonObjectElementTreeImpl element = element();

    assertThat(element.getKind()).isEqualTo(Kind.OBJECT_ELEMENT);
    assertThat(element.is(Kind.OBJECT_ELEMENT)).isTrue();
  }

  @Test
  void shouldMatchObjectElementAmongMultipleKinds() {
    JsonObjectElementTreeImpl element = element();

    assertThat(element.is(Kind.OBJECT, Kind.OBJECT_ELEMENT, Kind.TUPLE)).isTrue();
  }

  @Test
  void shouldNotMatchUnrelatedKinds() {
    JsonObjectElementTreeImpl element = element();

    assertThat(element.is(Kind.OBJECT)).isFalse();
    assertThat(element.is(Kind.OBJECT, Kind.STRING_LITERAL, Kind.TUPLE)).isFalse();
    assertThat(element.is()).isFalse();
  }

  private static JsonObjectElementTreeImpl element() {
    return new JsonObjectElementTreeImpl(literal("k"), new JsonSyntaxTokenImpl(":", RANGE), literal("v"), RANGE);
  }

  private static JsonLiteralExprTreeImpl literal(String value) {
    return new JsonLiteralExprTreeImpl(Kind.STRING_LITERAL, value, new JsonSyntaxTokenImpl(value, RANGE), RANGE);
  }
}
