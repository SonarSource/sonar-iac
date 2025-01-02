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
package org.sonar.iac.jvmframeworkconfig.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScalarImplTest {

  @Test
  void constructorShouldFunctionCorrectly() {
    SyntaxTokenImpl syntaxToken = new SyntaxTokenImpl("testValue", TextRanges.range(1, 2, 3, 4));
    ScalarImpl scalarTree = new ScalarImpl(syntaxToken);

    assertThat(scalarTree.value()).isEqualTo(syntaxToken);
    assertThat(scalarTree.children()).hasSize(1);
    assertThat(scalarTree.children()).containsOnly(syntaxToken);

    TextRangeAssert.assertThat(scalarTree.textRange())
      .hasRange(1, 2, 3, 4);
  }

  @Test
  void constructorTestWhenNull() {
    ScalarImpl scalarTree = new ScalarImpl(null);

    assertThat(scalarTree.value()).isNull();
    assertThatThrownBy(scalarTree::children).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(scalarTree::textRange).isInstanceOf(NullPointerException.class);
  }
}
