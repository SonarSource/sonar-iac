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
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class TupleImplTest {

  @Test
  void constructorShouldFunctionCorrectly() {
    SyntaxTokenImpl keySyntaxToken = new SyntaxTokenImpl("keySyntaxToken", TextRanges.range(1, 3, 1, 7));
    ScalarImpl keyScalar = new ScalarImpl(keySyntaxToken);

    SyntaxTokenImpl valueSyntaxToken = new SyntaxTokenImpl("valueSyntaxToken", TextRanges.range(1, 10, 1, 20));
    ScalarImpl valueScalar = new ScalarImpl(valueSyntaxToken);

    TupleImpl tupleTree = new TupleImpl(keyScalar, valueScalar);

    assertThat(tupleTree.key()).isEqualTo(keyScalar);
    assertThat(tupleTree.value()).isEqualTo(valueScalar);

    assertThat(tupleTree.children()).hasSize(2);
    assertThat(tupleTree.children()).containsExactly(keyScalar, valueScalar);

    TextRangeAssert.assertThat(tupleTree.textRange())
      .hasRange(1, 3, 1, 20);
  }

  @Test
  void constructorTestWhenValueNull() {
    SyntaxTokenImpl keySyntaxToken = new SyntaxTokenImpl("keySyntaxToken", TextRanges.range(1, 3, 1, 7));
    ScalarImpl keyScalar = new ScalarImpl(keySyntaxToken);
    TupleImpl tupleTree = new TupleImpl(keyScalar, null);

    assertThat(tupleTree.key()).isEqualTo(keyScalar);
    assertThat(tupleTree.value()).isNull();

    assertThat(tupleTree.children()).hasSize(1);
    assertThat(tupleTree.children()).containsExactly(keyScalar);

    assertThatNoException().isThrownBy(tupleTree::textRange);
  }
}
