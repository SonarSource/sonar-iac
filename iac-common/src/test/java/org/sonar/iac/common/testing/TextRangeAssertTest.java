/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.testing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

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
    assertTextRange(range).hasRange(1, 2, 3, 4);
  }

  @Test
  void range_failure() {
    assertThrows(AssertionError.class,
      () -> assertTextRange(range).hasRange(1, 2, 3, 5));
  }

}
