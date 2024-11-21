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
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.typed.Input;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.parser.grammar.DockerKeyword;

import static org.sonar.iac.common.parser.grammar.LexicalConstant.WHITESPACE;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalConstant.EOL;

public class DockerPreprocessor {

  private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<>();
  private static final String NOT_EOL_CHARS = "[^\\n\\r\\u2028\\u2029]";
  private static final String COMMENT = "(#" + NOT_EOL_CHARS + "*+)";
  private static final String INLINE_COMMENT_OR_EMPTY_LINE = "(?<!" + NOT_EOL_CHARS + ")(?<inlineCommentOrEmptyLine>(?:[" + WHITESPACE + "]*+" + COMMENT + "?(?:" + EOL + "|$))*)";
  private static final String COMMENT_LINE = "(?<!" + NOT_EOL_CHARS + ")(?<commentLine>(?:[" + WHITESPACE + "]*+" + COMMENT + "(?:" + EOL + "|$)))";

  static final String DEFAULT_ESCAPE_CHAR = "\\\\";
  static final String ALTERNATIVE_ESCAPE_CHAR = "`";
  private static final String ALTERNATIVE_ESCAPE_CHAR_DIRECTIVE = "#\\s*+escape\\s*+=\\s*+" + ALTERNATIVE_ESCAPE_CHAR;
  private static final Pattern ALTERNATIVE_ESCAPE_CHAR_PATTERN = Pattern.compile("^(#[^" + EOL + "]*+" + EOL + "|\\s)*" + ALTERNATIVE_ESCAPE_CHAR_DIRECTIVE);
  private static final Pattern COMMENT_PATTERN = Pattern.compile(COMMENT);

  // Extract list of docker instructions from DockerKeyword
  private static final List<String> DOCKER_INSTRUCTIONS = Arrays.stream(DockerKeyword.values()).map(DockerKeyword::getValue).toList();
  /**
   * Regex to remove prefix {@code CROSS_BUILD_} according to strict rules to avoid FP as much as possible:
   * - it must prefix a valid dockerfile instruction
   * - it must be at the beginning of a line/file
   * - the full instruction must be followed by a whitespace
   */
  private static final String CROSS_BUILD_REGEX = "(?<=^|[\r\n])CROSS_BUILD_(?=(?:" + String.join("|", DOCKER_INSTRUCTIONS) + ")[" + WHITESPACE + "])";

  private static final Set<String> COMMENT_TYPES = Set.of("commentLine", "inlineCommentOrEmptyLine");

  /**
   * Remove every escaped line break. This results in instructions being represented in one line at a time.
   * Track removed characters to adjust the offset when creating syntax tokens.
   * Also remove prefix on instructions starting with {@code CROSS_BUILD_}. This is an unofficial feature that is widely used.
   */
  public PreprocessorResult process(String source) {
    Input input = new Input(source.toCharArray());
    SortedMap<Integer, Integer> shiftedOffsetMap = new TreeMap<>();
    SortedMap<Integer, Comment> comments = new TreeMap<>();
    StringBuilder sb = new StringBuilder(source);

    int shiftedIndex = 0;

    Matcher m = matchRemovableSequences(source);
    while (m.find()) {
      // Remove sequence from source code
      int startIndex = m.start() - shiftedIndex;
      int removableLength = m.end() - m.start();
      sb.delete(startIndex, startIndex + removableLength);
      shiftedIndex += removableLength;

      // Process comments
      COMMENT_TYPES.forEach(type -> extractComments(input, m, comments, type));

      // Update offset map
      shiftedOffsetMap.put(m.end() - shiftedIndex, shiftedIndex);
    }

    SourceOffset sourceOffset = new SourceOffset(input, shiftedOffsetMap);
    return new PreprocessorResult(sb.toString(), sourceOffset, comments);
  }

  private static void extractComments(Input input, Matcher sourceMatcher, SortedMap<Integer, Comment> commentMap, String commentType) {
    String commentLine = sourceMatcher.group(commentType);
    if (commentLine != null) {
      Matcher commentMatcher = COMMENT_PATTERN.matcher(commentLine);
      while (commentMatcher.find()) {
        int[] lineAndColumn = input.lineAndColumnAt(sourceMatcher.start(commentType) + commentMatcher.start());
        Comment comment = buildComment(commentMatcher.group(), lineAndColumn[0], lineAndColumn[1] - 1);
        commentMap.put(lineAndColumn[0], comment);
      }
    }
  }

  private static Comment buildComment(String value, int line, int column) {
    TextRange range = TextRanges.range(line, column, value);
    String contentText = value.length() > 1 ? value.substring(1).trim() : "";
    return new CommentImpl(value, contentText, range);
  }

  private static Matcher matchRemovableSequences(String source) {
    String escapeCharacter = determineEscapeCharacter(source);
    return PATTERN_CACHE.computeIfAbsent(escapeCharacter, DockerPreprocessor::computePattern).matcher(source);
  }

  private static Pattern computePattern(String escapeCharacter) {
    String escapedLineBreaks = "(?<escapedLineBreaks>(?<!escape=)" + escapeCharacter + "[" + WHITESPACE + "]*+" + EOL + ")";
    String multiLineInstruction = escapedLineBreaks + INLINE_COMMENT_OR_EMPTY_LINE;

    String pattern = "(?:" + multiLineInstruction + "|" + COMMENT_LINE + "|" + CROSS_BUILD_REGEX + ")";

    return Pattern.compile(pattern);
  }

  static String determineEscapeCharacter(String source) {
    return ALTERNATIVE_ESCAPE_CHAR_PATTERN.matcher(source).find() ? ALTERNATIVE_ESCAPE_CHAR : DEFAULT_ESCAPE_CHAR;
  }

  public static class SourceOffset {

    protected final Input input;
    protected final Iterator<Map.Entry<Integer, Integer>> shiftedOffsetIterator;

    private int currentOffsetAdjustment = 0;
    private int nextOffsetAdjustment = 0;
    private int nextIndexOffset = 0;

    public SourceOffset(Input input, SortedMap<Integer, Integer> shiftedOffsetMap) {
      this.input = input;
      shiftedOffsetIterator = shiftedOffsetMap.entrySet().iterator();
      if (shiftedOffsetIterator.hasNext()) {
        moveToNextOffset();
      }
    }

    /**
     * Adjust index of parsed token to reflect actual location in the code.
     * This adjustment only works in ascending order, i.e. the indices must be the same or higher for each call.
     * This is done for performance reasons.
     */
    public int[] sourceLineAndColumnAt(int index) {
      return input.lineAndColumnAt(adjustIndex(index));
    }

    public int adjustIndex(int index) {
      while (nextIndexOffset <= index) {
        currentOffsetAdjustment = nextOffsetAdjustment;
        if (shiftedOffsetIterator.hasNext()) {
          moveToNextOffset();
        } else {
          break;
        }
      }
      return index + currentOffsetAdjustment;
    }

    private void moveToNextOffset() {
      Map.Entry<Integer, Integer> nextOffset = shiftedOffsetIterator.next();
      nextIndexOffset = nextOffset.getKey();
      nextOffsetAdjustment = nextOffset.getValue();
    }
  }

  static class PreprocessorResult {

    private final String processedSourceCode;
    private final SourceOffset sourceOffset;
    private final SortedMap<Integer, Comment> commentMap;

    public PreprocessorResult(String processedSourceCode, SourceOffset sourceOffset, SortedMap<Integer, Comment> commentMap) {
      this.processedSourceCode = processedSourceCode;
      this.sourceOffset = sourceOffset;
      this.commentMap = commentMap;
    }

    public String processedSourceCode() {
      return processedSourceCode;
    }

    public SourceOffset sourceOffset() {
      return sourceOffset;
    }

    public SortedMap<Integer, Comment> commentMap() {
      return commentMap;
    }
  }
}
