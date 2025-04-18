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
package org.sonar.iac.common.testing;

import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.sonar.api.batch.fs.TextRange;

/**
 * This Assert class is for {@link org.sonar.api.batch.fs.TextRange}.
 * For {@link org.sonar.iac.common.api.tree.impl.TextRange} see {@link TextRangeAssert}.
 */
public class TextRangePluginApiAssert extends AbstractAssert<TextRangePluginApiAssert, TextRange> {

  private TextRangePluginApiAssert(@Nullable TextRange actual) {
    super(actual, TextRangePluginApiAssert.class);
  }

  public static TextRangePluginApiAssert assertThat(@Nullable TextRange actual) {
    return new TextRangePluginApiAssert(actual);
  }

  public TextRangePluginApiAssert hasRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
    isNotNull();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(actual.start().line()).as("startLine mismatch").isEqualTo(startLine);
      softly.assertThat(actual.start().lineOffset()).as("startLineOffset mismatch").isEqualTo(startLineOffset);
      softly.assertThat(actual.end().line()).as("endLine mismatch").isEqualTo(endLine);
      softly.assertThat(actual.end().lineOffset()).as("endLineOffset mismatch").isEqualTo(endLineOffset);
    });
    return this;
  }

  public TextRangePluginAndCodeAssert on(String sourceCode) {
    return new TextRangePluginAndCodeAssert(actual, sourceCode);
  }

  public static class TextRangePluginAndCodeAssert extends AbstractAssert<TextRangePluginAndCodeAssert, TextRange> {

    private final String sourceCode;

    protected TextRangePluginAndCodeAssert(TextRange textRange, String sourceCode) {
      super(textRange, TextRangePluginAndCodeAssert.class);
      this.sourceCode = sourceCode;
    }

    public TextRangePluginAndCodeAssert hasRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
      isNotNull();
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(actual.start().line()).as("startLine mismatch").isEqualTo(startLine);
        softly.assertThat(actual.start().lineOffset()).as("startLineOffset mismatch").isEqualTo(startLineOffset);
        softly.assertThat(actual.end().line()).as("endLine mismatch").isEqualTo(endLine);
        softly.assertThat(actual.end().lineOffset()).as("endLineOffset mismatch").isEqualTo(endLineOffset);
      });
      return this;
    }

    /**
     * It's simplified implementation, it assumes only \n as new line character.
     */
    @Override
    public TextRangePluginAndCodeAssert isEqualTo(Object expected) {
      var expectedText = (String) expected;
      var lines = sourceCode.split("\n");
      var startLine = actual.start().line();
      var endLine = actual.end().line();
      if (startLine == endLine) {
        var actualText = lines[startLine - 1].substring(actual.start().lineOffset(), actual.end().lineOffset());
        Assertions.assertThat(actualText).isEqualTo(expectedText);
      } else {
        var sb = new StringBuilder();
        var first = lines[startLine - 1].substring(actual.start().lineOffset(), actual.end().lineOffset());
        sb.append(first);
        for (int i = startLine + 1; i < endLine; i++) {
          sb.append(lines[i - 1]);
        }
        var last = lines[endLine - 1].substring(0, actual.end().lineOffset());
        sb.append(last);
        Assertions.assertThat(sb).hasToString(expectedText);
      }
      return this;
    }
  }
}
