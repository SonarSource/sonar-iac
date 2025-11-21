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
package org.sonar.iac.docker.checks;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentList;
import org.sonar.iac.docker.tree.api.ExpandableStringCharacters;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

@Rule(key = "S7020")
public class LongRunInstructionCheck implements IacCheck {

  public static final int DEFAULT_MAX_LENGTH = 120;
  public static final int MIN_WORD_TO_TRIGGER = 7;
  private static final String MESSAGE = "Split this RUN instruction line into multiple lines.";
  private static final Predicate<String> HAS_URL = Pattern.compile("\\b((https?|ftp)://|(www|ftp)\\.)\\S++").asPredicate();

  @RuleProperty(
    key = "maxLength",
    description = "The maximum length that should not be exceeded by any RUN line instruction.",
    defaultValue = "" + DEFAULT_MAX_LENGTH)
  public int maxLength = DEFAULT_MAX_LENGTH;

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, this::checkInstruction);
  }

  private void checkInstruction(CheckContext ctx, RunInstruction runInstruction) {
    List<TooLongLine> tooLongLines = computeTooLongLines(runInstruction);
    for (TooLongLine tooLongLine : tooLongLines) {
      ctx.reportIssue(TextRanges.range(tooLongLine.line, tooLongLine.startOffset, tooLongLine.line, tooLongLine.endOffset), MESSAGE);
    }
  }

  private List<TooLongLine> computeTooLongLines(RunInstruction runInstruction) {
    List<TooLongLine> result = new ArrayList<>();
    var runInstructionData = computeRunInstructionDataPerLines(runInstruction);

    for (Map.Entry<Integer, Integer> linesWithOffsets : runInstructionData.tooLongLinesWithLastOffset.entrySet()) {
      int line = linesWithOffsets.getKey();
      if (runInstructionData.wordsPerLine.getOrDefault(line, 0) >= MIN_WORD_TO_TRIGGER
        && !runInstructionData.linesWithUrl.contains(line)) {
        int startOffset = runInstructionData.firstOffsetPerLine.get(line);
        int endOffset = linesWithOffsets.getValue();
        result.add(new TooLongLine(line, startOffset, endOffset));
      }
    }
    return result;
  }

  /**
   * Gather data about a {@link RunInstruction}:
   * - the first offset of each line, required to raise issue precisely
   * - the too long lines number with the last offset exceeding the {@link #maxLength} value
   * - the number of words per lines, to not raise an issue when below the {@link #MIN_WORD_TO_TRIGGER} value
   */
  private RunInstructionData computeRunInstructionDataPerLines(RunInstruction runInstruction) {
    Map<Integer, Integer> wordsPerLine = countWordsPerLine(runInstruction);
    Set<Integer> linesWithUrl = getLinesWithUrl(runInstruction);
    var runInstructionData = new RunInstructionData(new HashMap<>(), new HashMap<>(), wordsPerLine, linesWithUrl);

    TreeVisitor<TreeContext> visitor = new TreeVisitor<>();
    visitor.register(SyntaxToken.class, (TreeContext ctx, SyntaxToken token) -> {
      runInstructionData.firstOffsetPerLine.computeIfAbsent(token.textRange().start().line(), line -> token.textRange().start().lineOffset());
      // Special case of token that finish on a different line: consider the first offset of the end line to start at 0.
      if (token.textRange().start().line() != token.textRange().end().line()) {
        runInstructionData.firstOffsetPerLine.put(token.textRange().end().line(), 0);
      }
      if (token.textRange().end().lineOffset() > maxLength) {
        runInstructionData.tooLongLinesWithLastOffset.put(token.textRange().end().line(), token.textRange().end().lineOffset());
      }
    });
    visitor.scan(new TreeContext(), runInstruction);

    return runInstructionData;
  }

  private static Map<Integer, Integer> countWordsPerLine(RunInstruction runInstruction) {
    Map<Integer, Integer> wordsPerLine = new HashMap<>();
    incrementWordCountOnLine(wordsPerLine, runInstruction.keyword());
    runInstruction.options().forEach(flag -> incrementWordCountOnLine(wordsPerLine, flag));
    if (runInstruction.code() instanceof SyntaxToken syntaxToken) {
      incrementWordCountOnLine(wordsPerLine, syntaxToken.textRange().start().line(), countWordsInString(syntaxToken.value()));
    } else if (runInstruction.code() instanceof ArgumentList argumentList) {
      argumentList.arguments().forEach(argument -> incrementWordCountOnLine(wordsPerLine, argument));
    }
    return wordsPerLine;
  }

  private static int countWordsInString(String text) {
    // 1. Fast fail for null or empty
    if (text.isEmpty()) {
      return 0;
    }
    int count = 0;
    boolean isWord = false;
    int length = text.length();
    for (int i = 0; i < length; i++) {
      char c = text.charAt(i);
      // 2. Check if the character is whitespace (equivalent to \s)
      if (Character.isWhitespace(c)) {
        isWord = false;
      } else if (!isWord) {
        // 3. If we weren't in a word, and now we are, increment count
        count++;
        isWord = true;
      }
    }
    return count;
  }

  private static void incrementWordCountOnLine(Map<Integer, Integer> wordsPerLine, Tree tree) {
    if (tree.textRange().start().line() != tree.textRange().end().line()) {
      incrementWordCountOnLine(wordsPerLine, tree.textRange().start().line());
    }
    incrementWordCountOnLine(wordsPerLine, tree.textRange().end().line());
  }

  private static void incrementWordCountOnLine(Map<Integer, Integer> wordsPerLine, int line) {
    wordsPerLine.merge(line, 1, Integer::sum);
  }

  private static void incrementWordCountOnLine(Map<Integer, Integer> wordsPerLine, int line, int wordCount) {
    wordsPerLine.merge(line, wordCount, Integer::sum);
  }

  private static Set<Integer> getLinesWithUrl(RunInstruction runInstruction) {
    if (runInstruction.code() instanceof SyntaxToken syntaxToken) {
      if (HAS_URL.test(syntaxToken.value())) {
        return Set.of(syntaxToken.textRange().start().line());
      }
    } else if (runInstruction.code() instanceof ArgumentList argumentList) {
      return argumentList.arguments().stream()
        .filter(LongRunInstructionCheck::isArgumentUrl)
        .flatMap((Argument urlArgument) -> {
          int startLine = urlArgument.textRange().start().line();
          int endLine = urlArgument.textRange().end().line();
          return IntStream.rangeClosed(startLine, endLine).boxed();
        })
        .collect(Collectors.toSet());
    }
    return Set.of();
  }

  private static boolean isArgumentUrl(Argument argument) {
    return argument.expressions().stream().anyMatch(LongRunInstructionCheck::isExpressionUrl);
  }

  private static boolean isExpressionUrl(Expression expression) {
    return switch (expression.getKind()) {
      case STRING_LITERAL -> isStringUrl(((Literal) expression).value());
      case EXPANDABLE_STRING_LITERAL -> ((ExpandableStringLiteral) expression).expressions().stream().anyMatch(LongRunInstructionCheck::isExpressionUrl);
      case EXPANDABLE_STRING_CHARACTERS -> isStringUrl(((ExpandableStringCharacters) expression).value());
      default -> false;
    };
  }

  private static boolean isStringUrl(String value) {
    try {
      new URL(value).toURI();
      return true;
    } catch (MalformedURLException | URISyntaxException e) {
      return false;
    }
  }

  record RunInstructionData(
    Map<Integer, Integer> firstOffsetPerLine,
    Map<Integer, Integer> tooLongLinesWithLastOffset,
    Map<Integer, Integer> wordsPerLine,
    Set<Integer> linesWithUrl) {
  }

  record TooLongLine(int line, int startOffset, int endOffset) {
  }
}
