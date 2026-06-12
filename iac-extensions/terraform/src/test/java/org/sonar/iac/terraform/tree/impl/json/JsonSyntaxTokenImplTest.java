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
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSyntaxTokenImplTest {

  private static final TextRange RANGE = new TextRange(new TextPointer(1, 0), new TextPointer(3, 5));

  @Test
  void shouldExposeValueAndRange() {
    JsonSyntaxTokenImpl token = new JsonSyntaxTokenImpl("{", RANGE);

    assertThat(token.value()).isEqualTo("{");
    assertThat(token.textRange()).isSameAs(RANGE);
  }

  @Test
  void shouldHaveNoCommentsOrChildren() {
    JsonSyntaxTokenImpl token = new JsonSyntaxTokenImpl(":", RANGE);

    assertThat(token.comments()).isEmpty();
    assertThat(token.children()).isEmpty();
  }

  @Test
  void shouldReportTokenKind() {
    JsonSyntaxTokenImpl token = new JsonSyntaxTokenImpl(",", RANGE);

    assertThat(token.getKind()).isEqualTo(Kind.TOKEN);
    assertThat(token.is(Kind.TOKEN)).isTrue();
  }

  @Test
  void shouldMatchTokenAmongMultipleKinds() {
    JsonSyntaxTokenImpl token = new JsonSyntaxTokenImpl("[", RANGE);

    assertThat(token.is(Kind.OBJECT, Kind.TOKEN, Kind.TUPLE)).isTrue();
  }

  @Test
  void shouldNotMatchUnrelatedKinds() {
    JsonSyntaxTokenImpl token = new JsonSyntaxTokenImpl("]", RANGE);

    assertThat(token.is(Kind.OBJECT)).isFalse();
    assertThat(token.is(Kind.OBJECT, Kind.TUPLE, Kind.STRING_LITERAL)).isFalse();
  }

  @Test
  void shouldNotMatchAnyKindWhenCalledWithEmptyArray() {
    JsonSyntaxTokenImpl token = new JsonSyntaxTokenImpl("=", RANGE);

    assertThat(token.is()).isFalse();
  }
}
