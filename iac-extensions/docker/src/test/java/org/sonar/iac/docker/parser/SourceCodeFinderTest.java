/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SourceCodeFinderTest {

  private SourceCodeFinder finder;

  @BeforeEach
  void setUp() {
    finder = new SourceCodeFinder();
  }

  @Test
  void shouldExtractSingleLineCode() {
    var source = "hello world";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 1, 11);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("hello world");
  }

  @Test
  void shouldExtractPartialLineFromStart() {
    var source = "hello world";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 1, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("hello");
  }

  @Test
  void shouldExtractPartialLineFromEnd() {
    var source = "hello world";
    finder.setSource(source);

    var token = createSyntaxToken(1, 6, 1, 11);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("world");
  }

  @Test
  void shouldExtractPartialLineFromMiddle() {
    var source = "hello world";
    finder.setSource(source);

    var token = createSyntaxToken(1, 3, 1, 7);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("lo w");
  }

  @Test
  void shouldExtractMultipleLines() {
    var source = "line1\nline2\nline3";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 3, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("line1\nline2\nline3");
  }

  @Test
  void shouldExtractPartialFirstLine() {
    var source = "line1\nline2\nline3";
    finder.setSource(source);

    var token = createSyntaxToken(1, 2, 3, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("ne1\nline2\nline3");
  }

  @Test
  void shouldExtractPartialLastLine() {
    var source = "line1\nline2\nline3";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 3, 3);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("line1\nline2\nlin");
  }

  @Test
  void shouldExtractMiddleLinesToPartialFirstAndLastLine() {
    var source = "line1\nline2\nline3\nline4";
    finder.setSource(source);

    var token = createSyntaxToken(1, 3, 4, 2);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("e1\nline2\nline3\nli");
  }

  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r\n", "\r"})
  void shouldDetectDifferentNewlineTypes(String newline) {
    var source = "line1" + newline + "line2";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 2, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("line1" + newline + "line2");
  }

  @Test
  void shouldCacheSourceLines() {
    var source = "line1\nline2\nline3";
    finder.setSource(source);

    assertThat(finder.cachedSourceLines).isNull();
    var token1 = createSyntaxToken(1, 0, 1, 5);
    var result1 = finder.findSourceCode(token1);
    var cache1 = finder.cachedSourceLines;

    var token2 = createSyntaxToken(2, 0, 2, 5);
    var result2 = finder.findSourceCode(token2);
    var cache2 = finder.cachedSourceLines;

    assertThat(result1).isEqualTo("line1");
    assertThat(result2).isEqualTo("line2");
    assertThat(cache1).isNotNull().isSameAs(cache2);
  }

  @Test
  void shouldClearCacheWhenSourceIsChanged() {
    var source1 = "original\ncode";
    finder.setSource(source1);

    var token1 = createSyntaxToken(1, 0, 1, 8);
    var result1 = finder.findSourceCode(token1);
    var cache1 = finder.cachedSourceLines;

    var source2 = "new\ncode";
    finder.setSource(source2);

    var token2 = createSyntaxToken(1, 0, 1, 3);
    var result2 = finder.findSourceCode(token2);
    var cache2 = finder.cachedSourceLines;

    assertThat(result1).isEqualTo("original");
    assertThat(result2).isEqualTo("new");
    assertThat(cache1).isNotNull().isNotEqualTo(cache2);
    assertThat(cache2).isNotNull();
  }

  @Test
  void shouldHandleSingleCharacterExtraction() {
    var source = "hello";
    finder.setSource(source);

    var token = createSyntaxToken(1, 2, 1, 3);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("l");
  }

  @Test
  void shouldHandleComplexMultilineExtraction() {
    var source = """
      FROM ubuntu:latest
      RUN echo "hello world"
      ENV VAR=value
      """;
    finder.setSource(source);

    // Extract from line 2, offset 4 to line 3, offset 8 (i.e., "echo "hello world"" + newline + "ENV V")
    var token = createSyntaxToken(2, 4, 3, 8);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("""
      echo "hello world"
      ENV VAR=""");
  }

  @Test
  void shouldHandleSourceWithMixedNewlines() {
    // Note: This is an edge case - mixed newlines
    var source = "line1\nline2\r\nline3\rline4";
    finder.setSource(source);

    // Should use the first newline type found (LF)
    var token = createSyntaxToken(1, 0, 4, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("line1\r\nline2\r\nline3\r\nline4");
  }

  @Test
  void shouldExtractFromSecondLine() {
    var source = "line1\nline2\nline3";
    finder.setSource(source);

    var token = createSyntaxToken(2, 0, 2, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("line2");
  }

  @Test
  void shouldExtractFromLastLine() {
    var source = "line1\nline2\nline3";
    finder.setSource(source);

    var token = createSyntaxToken(3, 0, 3, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("line3");
  }

  @Test
  void shouldHandleSourceWithTrailingNewline() {
    var source = "line1\nline2\n";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 2, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("line1\nline2");
  }

  @Test
  void shouldHandleSourceWithOnlyNewlines() {
    var source = "\n\n\n";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 3, 0);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("\n\n");
  }

  @Test
  void shouldHandleSourceWithTabsAndSpaces() {
    var source = "  \t  hello\t\tworld  ";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 1, 19);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("  \t  hello\t\tworld  ");
  }

  @Test
  void shouldHandleSourceWithSpecialCharacters() {
    var source = "RUN echo \"$VAR\" && test -f /path/to/file";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 1, 40);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("RUN echo \"$VAR\" && test -f /path/to/file");
  }

  @Test
  void shouldExtractBashCommentLine() {
    var source = "#!/bin/bash\n# This is a comment\necho hello";
    finder.setSource(source);

    var token = createSyntaxToken(2, 0, 2, 19);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("# This is a comment");
  }

  @Test
  void shouldHandleZeroLengthRange() {
    var source = "hello world";
    finder.setSource(source);

    var token = createSyntaxToken(1, 5, 1, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldHandleUnicodeCharacters() {
    var source = "echo 'こんにちは世界'";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 1, 14);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("echo 'こんにちは世界'");
  }

  @Test
  void shouldPreferCRLFWhenBothCRAndLFExist() {
    var source = "line1\r\nline2\nline3";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 3, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("line1\r\nline2\r\nline3");
  }

  @Test
  void shouldHandleSourceWithMultipleConsecutiveNewlines() {
    var source = "line1\n\n\nline2";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 4, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("line1\n\n\nline2");
  }

  @Test
  void shouldThrowExceptionOnInvalidTextRange() {
    var source = "line1\nline2";
    finder.setSource(source);

    var token = createSyntaxToken(1, 0, 2, 8);
    assertThatThrownBy(() -> finder.findSourceCode(token))
      .isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  void shouldReturnSourceWithEmptyLinesIfTextRangeEndLineIsOutOfSource() {
    var source = "a\nb";
    finder.setSource(source);
    var token = createSyntaxToken(1, 0, 4, 5);
    var result = finder.findSourceCode(token);

    assertThat(result).isEqualTo("a\nb\n\n");
  }

  @Test
  void shouldReturnLineSeparatorWhenSourceIsEmpty() {
    var source = "";
    finder.setSource(source);
    assertThat(finder.lineSeparator()).isEqualTo("\n");
  }

  // Helper method to create a mock SyntaxToken with a specific text range
  private SyntaxToken createSyntaxToken(int startLine, int startOffset, int endLine, int endOffset) {
    var token = mock(SyntaxToken.class);
    var textRange = new TextRange(
      new TextPointer(startLine, startOffset),
      new TextPointer(endLine, endOffset));
    when(token.textRange()).thenReturn(textRange);
    return token;
  }
}
