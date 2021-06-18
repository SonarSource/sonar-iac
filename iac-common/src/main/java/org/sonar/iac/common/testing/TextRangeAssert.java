/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.testing;

import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.sonar.api.batch.fs.TextRange;

import static org.assertj.core.api.Assertions.assertThat;

public class TextRangeAssert extends AbstractAssert<TextRangeAssert, TextRange> {

  public TextRangeAssert(@Nullable TextRange actual) {
    super(actual, TextRangeAssert.class);
  }

  public TextRangeAssert hasRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
    isNotNull();
    assertThat(actual.start().line()).as("startLine mismatch").isEqualTo(startLine);
    assertThat(actual.start().lineOffset()).as("startLineOffset mismatch").isEqualTo(startLineOffset);
    assertThat(actual.end().line()).as("endLine mismatch").isEqualTo(endLine);
    assertThat(actual.end().lineOffset()).as("endLineOffset mismatch").isEqualTo(endLineOffset);
    return this;
  }

  public static TextRangeAssert assertTextRange(@Nullable TextRange actual) {
    return new TextRangeAssert(actual);
  }
}
