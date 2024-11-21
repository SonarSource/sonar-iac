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
package org.sonar.iac.jvmframeworkconfig.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SyntaxTokenImplTest {

  @Test
  void constructorShouldFunctionCorrectly() {
    TextRange range = TextRanges.range(1, 2, 3, 4);
    String value = "testValue";
    SyntaxTokenImpl syntaxToken = new SyntaxTokenImpl(value, range);

    assertThat(syntaxToken.value()).isEqualTo(value);
    assertThat(syntaxToken.textRange()).isEqualTo(range);
    assertThat(syntaxToken.children()).isEmpty();
  }

  @Test
  void constructorTestWhenNull() {
    SyntaxTokenImpl syntaxToken = new SyntaxTokenImpl(null, null);

    assertThat(syntaxToken.value()).isNull();
    assertThat(syntaxToken.children()).isEmpty();
    assertThatThrownBy(syntaxToken::textRange)
      .withFailMessage("Can't merge 0 ranges")
      .isInstanceOf(IllegalArgumentException.class);
  }
}
