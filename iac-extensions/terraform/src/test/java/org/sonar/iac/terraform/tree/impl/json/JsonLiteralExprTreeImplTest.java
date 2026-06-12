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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class JsonLiteralExprTreeImplTest {

  private static final TextRange RANGE = new TextRange(new TextPointer(1, 0), new TextPointer(2, 4));

  @Test
  void shouldExposeValueTokenAndRange() {
    SyntaxToken token = new JsonSyntaxTokenImpl("hello", RANGE);
    JsonLiteralExprTreeImpl literal = new JsonLiteralExprTreeImpl(Kind.STRING_LITERAL, "hello", token, RANGE);

    assertThat(literal.value()).isEqualTo("hello");
    assertThat(literal.token()).isSameAs(token);
    assertThat(literal.textRange()).isSameAs(RANGE);
  }

  @Test
  void shouldExposeTokenAsSingleChild() {
    SyntaxToken token = new JsonSyntaxTokenImpl("v", RANGE);
    JsonLiteralExprTreeImpl literal = new JsonLiteralExprTreeImpl(Kind.STRING_LITERAL, "v", token, RANGE);

    assertThat(literal.children()).containsExactly(token);
  }

  @Test
  void shouldKeepValueAndTokenIndependent() {
    // The constructor stores value separately from the token's value; callers can pass distinct strings.
    SyntaxToken token = new JsonSyntaxTokenImpl("\"raw\"", RANGE);
    JsonLiteralExprTreeImpl literal = new JsonLiteralExprTreeImpl(Kind.STRING_LITERAL, "unquoted", token, RANGE);

    assertThat(literal.value()).isEqualTo("unquoted");
    assertThat(literal.token().value()).isEqualTo("\"raw\"");
  }

  /**
   * The kind passed to the constructor must round-trip through {@link JsonLiteralExprTreeImpl#getKind()}
   * and {@link JsonLiteralExprTreeImpl#is(Kind...)}. Each JSON scalar carries its own kind so checks
   * gating on {@code is(Kind.BOOLEAN_LITERAL)} etc. behave the same way against heredoc-embedded JSON
   * as against native HCL.
   */
  @ParameterizedTest
  @EnumSource(value = Kind.class, names = {"STRING_LITERAL", "BOOLEAN_LITERAL", "NUMERIC_LITERAL", "NULL_LITERAL"})
  void shouldRoundTripScalarKinds(Kind kind) {
    JsonLiteralExprTreeImpl literal = new JsonLiteralExprTreeImpl(kind, "x", new JsonSyntaxTokenImpl("x", RANGE), RANGE);

    assertThat(literal.getKind()).isEqualTo(kind);
    assertThat(literal.is(kind)).isTrue();
  }

  @Test
  void shouldMatchOwnKindAmongMultipleKinds() {
    JsonLiteralExprTreeImpl literal = new JsonLiteralExprTreeImpl(Kind.BOOLEAN_LITERAL, "true", new JsonSyntaxTokenImpl("true", RANGE), RANGE);

    assertThat(literal.is(Kind.OBJECT, Kind.BOOLEAN_LITERAL, Kind.STRING_LITERAL)).isTrue();
  }

  @Test
  void shouldNotMatchADifferentLiteralKind() {
    // A BOOLEAN_LITERAL must not pretend to be any other literal kind — that's the whole point of
    // carrying the kind explicitly rather than collapsing everything to STRING_LITERAL.
    JsonLiteralExprTreeImpl bool = new JsonLiteralExprTreeImpl(Kind.BOOLEAN_LITERAL, "true", new JsonSyntaxTokenImpl("true", RANGE), RANGE);

    assertThat(bool.is(Kind.STRING_LITERAL)).isFalse();
    assertThat(bool.is(Kind.NUMERIC_LITERAL, Kind.NULL_LITERAL)).isFalse();
  }

  @Test
  void shouldNotMatchUnrelatedKinds() {
    JsonLiteralExprTreeImpl literal = new JsonLiteralExprTreeImpl(Kind.STRING_LITERAL, "x", new JsonSyntaxTokenImpl("x", RANGE), RANGE);

    assertThat(literal.is(Kind.OBJECT, Kind.TUPLE)).isFalse();
    assertThat(literal.is()).isFalse();
  }
}
