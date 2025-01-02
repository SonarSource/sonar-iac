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
package org.sonar.iac.common.extension;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThat;

class BasicTextPointerTest {

  @Test
  void shouldCreateBasicTextPointerFromTextRange() {
    TextRange range = TextRanges.range(2, 5, 10, 15);
    BasicTextPointer textPointer = new BasicTextPointer(range);
    assertThat(textPointer.line()).isEqualTo(2);
    assertThat(textPointer.lineOffset()).isEqualTo(4);
  }

  @Test
  void shouldCompareBasicTextPointers() {
    BasicTextPointer pointer1 = new BasicTextPointer(1, 5);
    BasicTextPointer pointer2 = new BasicTextPointer(3, 0);

    assertThat(pointer1).isEqualByComparingTo(pointer1);
    assertThat(pointer1.compareTo(pointer2)).isEqualTo(-1);
    assertThat(pointer2.compareTo(pointer1)).isEqualTo(1);
  }
}
