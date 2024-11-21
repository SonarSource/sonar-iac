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
package org.sonar.iac.common.testing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class TextRangeAssertTest {

  private TextRange range;

  @BeforeEach
  void setup() {
    TextPointer start = mock(TextPointer.class);
    when(start.line()).thenReturn(1);
    when(start.lineOffset()).thenReturn(2);

    TextPointer end = mock(TextPointer.class);
    when(end.line()).thenReturn(3);
    when(end.lineOffset()).thenReturn(4);

    range = mock(TextRange.class);
    when(range.start()).thenReturn(start);
    when(range.end()).thenReturn(end);
  }

  @Test
  void range_ok() {
    assertThat(range).hasRange(1, 2, 3, 4);
  }

  @Test
  void range_failure() {
    TextRangeAssert assertion = assertThat(range);
    assertThrows(AssertionError.class,
      () -> assertion.hasRange(1, 2, 3, 5));
  }

}
