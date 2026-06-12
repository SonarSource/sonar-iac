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
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.tree.impl.SeparatedTreesImpl;

import static org.assertj.core.api.Assertions.assertThat;

class JsonObjectTreeImplTest {

  private static final TextRange RANGE = new TextRange(new TextPointer(1, 0), new TextPointer(4, 0));

  @Test
  void shouldExposeElementsPropertiesAndRange() {
    JsonObjectElementTreeImpl element = stringMember("k", "v");
    SeparatedTrees<ObjectElementTree> elements = new SeparatedTreesImpl<>(List.of(element), List.of());
    SyntaxToken open = new JsonSyntaxTokenImpl("{", RANGE);
    SyntaxToken close = new JsonSyntaxTokenImpl("}", RANGE);

    JsonObjectTreeImpl object = new JsonObjectTreeImpl(elements, open, close, RANGE);

    assertThat(object.elements()).isSameAs(elements);
    assertThat(object.properties()).containsExactly(element);
    assertThat(object.textRange()).isSameAs(RANGE);
  }

  @Test
  void shouldExposeChildrenAsBracesWithElementsAndSeparators() {
    JsonObjectElementTreeImpl first = stringMember("a", "1");
    JsonObjectElementTreeImpl second = stringMember("b", "2");
    SyntaxToken separator = new JsonSyntaxTokenImpl(",", RANGE);
    SeparatedTrees<ObjectElementTree> elements = new SeparatedTreesImpl<>(List.of(first, second), List.of(separator));
    SyntaxToken open = new JsonSyntaxTokenImpl("{", RANGE);
    SyntaxToken close = new JsonSyntaxTokenImpl("}", RANGE);

    JsonObjectTreeImpl object = new JsonObjectTreeImpl(elements, open, close, RANGE);

    assertThat(object.children()).containsExactly(open, first, separator, second, close);
  }

  @Test
  void shouldExposeEmptyChildrenBetweenBracesForEmptyObject() {
    SeparatedTrees<ObjectElementTree> elements = new SeparatedTreesImpl<>(List.of(), List.of());
    SyntaxToken open = new JsonSyntaxTokenImpl("{", RANGE);
    SyntaxToken close = new JsonSyntaxTokenImpl("}", RANGE);

    JsonObjectTreeImpl object = new JsonObjectTreeImpl(elements, open, close, RANGE);

    assertThat(object.properties()).isEmpty();
    assertThat(object.children()).containsExactly(open, close);
  }

  @Test
  void shouldReportObjectKind() {
    JsonObjectTreeImpl object = emptyObject();

    assertThat(object.getKind()).isEqualTo(Kind.OBJECT);
    assertThat(object.is(Kind.OBJECT)).isTrue();
  }

  @Test
  void shouldMatchObjectAmongMultipleKinds() {
    JsonObjectTreeImpl object = emptyObject();

    assertThat(object.is(Kind.TUPLE, Kind.OBJECT, Kind.STRING_LITERAL)).isTrue();
  }

  @Test
  void shouldNotMatchUnrelatedKinds() {
    JsonObjectTreeImpl object = emptyObject();

    assertThat(object.is(Kind.TUPLE)).isFalse();
    assertThat(object.is(Kind.OBJECT_ELEMENT, Kind.TUPLE, Kind.STRING_LITERAL)).isFalse();
    assertThat(object.is()).isFalse();
  }

  private static JsonObjectTreeImpl emptyObject() {
    SeparatedTrees<ObjectElementTree> elements = new SeparatedTreesImpl<>(List.of(), List.of());
    return new JsonObjectTreeImpl(elements, new JsonSyntaxTokenImpl("{", RANGE), new JsonSyntaxTokenImpl("}", RANGE), RANGE);
  }

  private static JsonObjectElementTreeImpl stringMember(String key, String value) {
    SyntaxToken keyToken = new JsonSyntaxTokenImpl(key, RANGE);
    SyntaxToken valueToken = new JsonSyntaxTokenImpl(value, RANGE);
    SyntaxToken colon = new JsonSyntaxTokenImpl(":", RANGE);
    return new JsonObjectElementTreeImpl(
      new JsonLiteralExprTreeImpl(Kind.STRING_LITERAL, key, keyToken, RANGE),
      colon,
      new JsonLiteralExprTreeImpl(Kind.STRING_LITERAL, value, valueToken, RANGE),
      RANGE);
  }
}
