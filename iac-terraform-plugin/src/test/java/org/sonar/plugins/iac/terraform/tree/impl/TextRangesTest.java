package org.sonar.plugins.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.api.tree.TextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class TextRangesTest {

  @Test
  void test_range() {
    TextRange range = TextRanges.range(1, 2, "value");
    assertThat(range).isEqualTo(new TextRangeImpl(1,2, 1, 7));
  }

  @Test
  void test_merge() {
    TextRange range1 = new TextRangeImpl(1,2, 3, 4);
    TextRange range2 = new TextRangeImpl(5,6, 7, 8);
    assertThat(TextRanges.merge(Arrays.asList(range1, range2)))
      .isEqualTo(new TextRangeImpl(1,2, 7, 8));
  }

  @Test
  void test_merge_single() {
    TextRange range1 = new TextRangeImpl(1,2, 3, 4);
    assertThat(TextRanges.merge(Collections.singletonList(range1)))
      .isEqualTo(new TextRangeImpl(1,2, 3, 4));
  }

  @Test
  void test_merge_no_range() {
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> TextRanges.merge(Collections.emptyList()))
      .withMessage("Can't merge 0 ranges");
  }

}
