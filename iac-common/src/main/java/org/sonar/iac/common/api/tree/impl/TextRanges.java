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
package org.sonar.iac.common.api.tree.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonarsource.analyzer.commons.TokenLocation;

import static java.util.Comparator.naturalOrder;

public class TextRanges {

  private static final Supplier<IllegalArgumentException> MERGE_EXCEPTION_SUPPLIER = () -> new IllegalArgumentException("Can't merge 0 ranges");

  private TextRanges() {
  }

  public static TextRange range(int startLine, int startColumn, int endLine, int endColumn) {
    return new TextRange(new TextPointer(startLine, startColumn), new TextPointer(endLine, endColumn));
  }

  public static TextRange range(int line, int column, String value) {
    TokenLocation location = new TokenLocation(line, column, value);
    TextPointer startPointer = new TextPointer(location.startLine(), location.startLineOffset());
    TextPointer endPointer = new TextPointer(location.endLine(), location.endLineOffset());
    return new TextRange(startPointer, endPointer);
  }

  public static TextRange merge(List<TextRange> ranges) {
    return new TextRange(
      ranges.stream().map(TextRange::start).min(naturalOrder()).orElseThrow(MERGE_EXCEPTION_SUPPLIER),
      ranges.stream().map(TextRange::end).max(naturalOrder()).orElseThrow(MERGE_EXCEPTION_SUPPLIER));
  }

  public static TextRange merge(TextRange... ranges) {
    return merge(Arrays.asList(ranges));
  }

  public static TextRange mergeElementsWithTextRange(List<? extends HasTextRange> elementsWithTextRange) {
    List<TextRange> textRanges = new ArrayList<>();
    for (HasTextRange element : elementsWithTextRange) {
      textRanges.add(element.textRange());
    }
    return TextRanges.merge(textRanges);
  }

  public static boolean isValidAndNotEmpty(TextRange range) {
    return range.end().compareTo(range.start()) > 0;
  }

  public static boolean endsBeforeAnotherEnds(HasTextRange target, HasTextRange base) {
    return target.textRange().end().compareTo(base.textRange().end()) <= 0;
  }

  public static <T> Comparator<T> comparingTextRangeStart(Function<T, HasTextRange> mapper) {
    return Comparator.comparingInt((T tree) -> mapper.apply(tree).textRange().start().line())
      .thenComparingInt(tree -> mapper.apply(tree).textRange().start().lineOffset());
  }
}
