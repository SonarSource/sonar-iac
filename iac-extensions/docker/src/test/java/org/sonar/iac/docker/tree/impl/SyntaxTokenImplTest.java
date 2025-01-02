/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.docker.tree.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class SyntaxTokenImplTest {
  @Test
  void shouldCheckEquality() {
    SyntaxToken token1 = new SyntaxTokenImpl("foo", range(1, 0, 1, 5), List.of());
    SyntaxToken token2 = new SyntaxTokenImpl("foo", range(1, 0, 1, 5), List.of());
    SyntaxToken token3 = new SyntaxTokenImpl("bar", range(1, 0, 1, 5), List.of());

    assertThat(token1)
      .isEqualTo(token1)
      .isEqualTo(token2)
      .hasSameHashCodeAs(token2)
      .isNotEqualTo(token3)
      .doesNotHaveSameHashCodeAs(token3)
      .isNotEqualTo(null)
      .isNotEqualTo(new Object());
  }
}
