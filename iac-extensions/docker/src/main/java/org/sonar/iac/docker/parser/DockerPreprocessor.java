/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.typed.Input;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.tree.Comment;

import static org.sonar.iac.common.parser.grammar.LexicalConstant.WHITESPACE;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalConstant.EOL;

public class DockerPreprocessor {

  static final String DEFAULT_ESCAPE_CHAR = "\\\\";
  static final String ALTERNATIVE_ESCAPE_CHAR = "`";
  private static final String ALTERNATIVE_ESCAPE_CHAR_DIRECTIVE = "#\\s*+escape\\s*+=\\s*+" + ALTERNATIVE_ESCAPE_CHAR;
  private static final Pattern ALTERNATIVE_ESCAPE_CHAR_PATTERN = Pattern.compile("^(#[^" + EOL + "]*+" + EOL + "|\\s)*" + ALTERNATIVE_ESCAPE_CHAR_DIRECTIVE);

  /**
   * Remove every escaped line break. This results in instructions being represented in one line at a time.
   * Track removed characters to adjust the offset when creating syntax tokens.
   */
  public PreprocessorResult process(String source) {
    Matcher m = matchEscapedLineBreaks(source);

    Map<Integer, Integer> shiftedOffsetMap = new LinkedHashMap<>();
    StringBuilder sb = new StringBuilder(source);
    int shiftedIndex = 0;

    while (m.find()) {
      int startIndex = m.start() - shiftedIndex;
      int linebreakLength = m.end() - m.start();
      for (int i = 0; i < linebreakLength; i++) {
        sb.deleteCharAt(startIndex);
        shiftedIndex++;
      }
      shiftedOffsetMap.put(m.end() - shiftedIndex, shiftedIndex);
    }

    String processedSourceCode = sb.toString();
    SourceOffset sourceOffset = new SourceOffset(source, shiftedOffsetMap);
    // TODO SONARIAC-533: Provide a Map of removed comments
    return new PreprocessorResult(processedSourceCode, sourceOffset, Collections.emptySortedMap());
  }

  private static Matcher matchEscapedLineBreaks(String source) {
    String escapeCharacter = determineEscapeCharacter(source);
    String escapedLineBreakPattern = "(?<!escape=)" + escapeCharacter + "[" + WHITESPACE + "]*+" + EOL;
    return Pattern.compile(escapedLineBreakPattern).matcher(source);
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

    public SourceOffset(String source, Map<Integer, Integer> shiftedOffsetMap) {
      input = new Input(source.toCharArray());
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

    private int adjustIndex(int index) {
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
