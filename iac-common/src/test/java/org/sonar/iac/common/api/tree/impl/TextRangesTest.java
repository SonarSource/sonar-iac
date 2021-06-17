/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.api.tree.impl;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.DefaultTextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class TextRangesTest {

  @Test
  void test_range() {
    TextRange range = TextRanges.range(1, 2, "value");
    assertThat(range).isEqualTo(TextRangesTest.range(1,2, 1, 7));
  }

  @Test
  void test_merge() {
    TextRange range1 = TextRangesTest.range(1,2, 3, 4);
    TextRange range2 = TextRangesTest.range(5,6, 7, 8);
    assertThat(TextRanges.merge(Arrays.asList(range1, range2)))
      .isEqualTo(TextRangesTest.range(1,2, 7, 8));
  }

  @Test
  void test_merge_single() {
    TextRange range1 = TextRangesTest.range(1,2, 3, 4);
    assertThat(TextRanges.merge(Collections.singletonList(range1)))
      .isEqualTo(TextRangesTest.range(1,2, 3, 4));
  }

  @Test
  void test_merge_no_range() {
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> TextRanges.merge(Collections.emptyList()))
      .withMessage("Can't merge 0 ranges");
  }

  public static TextRange range(int startLine, int startLineColumn, int endLine, int endLineColumn) {
    return new DefaultTextRange(new DefaultTextPointer(startLine, startLineColumn), new DefaultTextPointer(endLine, endLineColumn));
  }
}
