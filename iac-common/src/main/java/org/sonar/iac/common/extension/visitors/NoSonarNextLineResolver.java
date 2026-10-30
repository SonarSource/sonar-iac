/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.extension.visitors;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import org.jspecify.annotations.Nullable;
import org.sonar.iac.common.api.tree.impl.TextRange;

/**
 * Computes the lines covered by issue suppression comments, as detected by {@link MetricsVisitor}, that are written alone on
 * their line. It is used where the line an issue is reported on cannot always hold a comment, for example a Dockerfile
 * instruction, a {@code .properties} entry or the content of a heredoc.
 * <p>
 * Such a comment covers the next line holding code, skipping blank and comment lines. When an element registered with
 * {@link #addElement(TextRange)} starts on that line, the comment covers every line of the element.
 */
public class NoSonarNextLineResolver {

  private final NavigableSet<Integer> codeLines = new TreeSet<>();
  private final Map<Integer, Integer> elementEndLines = new HashMap<>();

  public void addCodeLine(int line) {
    codeLines.add(line);
  }

  public void addCodeLines(Collection<Integer> lines) {
    codeLines.addAll(lines);
  }

  public void addCodeLines(TextRange range) {
    for (int line = range.start().line(); line <= range.end().line(); line++) {
      codeLines.add(line);
    }
  }

  /**
   * Registers a multi-line element, such as an instruction, which a suppression comment written right above it covers as a whole.
   * When several elements start on the same line, the longest one is kept.
   */
  public void addElement(TextRange range) {
    elementEndLines.merge(range.start().line(), range.end().line(), Math::max);
  }

  /**
   * Returns the lines covered by the given suppression comment lines, besides the comment lines themselves.
   * A comment line that also holds code covers nothing more: a suppression comment after code only applies to its own line.
   */
  public Set<Integer> coveredLines(Collection<Integer> noSonarLines) {
    Set<Integer> coveredLines = new HashSet<>();
    for (int noSonarLine : noSonarLines) {
      var nextCodeLine = nextCodeLine(noSonarLine);
      if (nextCodeLine != null) {
        addLines(coveredLines, nextCodeLine, elementEndLines.getOrDefault(nextCodeLine, nextCodeLine));
      }
    }
    return coveredLines;
  }

  /**
   * Returns the lines of the registered elements covered by the given suppression comment lines. An element is covered by a
   * suppression comment on the line where it starts, or by one written alone on its line right above it.
   * Contrary to {@link #coveredLines(Collection)}, a suppression comment never covers the next line of code outside an element.
   */
  public Set<Integer> coveredElementLines(Collection<Integer> noSonarLines) {
    Set<Integer> coveredLines = new HashSet<>();
    for (int noSonarLine : noSonarLines) {
      var elementStartLine = elementEndLines.containsKey(noSonarLine) ? Integer.valueOf(noSonarLine) : nextCodeLine(noSonarLine);
      if (elementStartLine != null && elementEndLines.containsKey(elementStartLine)) {
        addLines(coveredLines, elementStartLine, elementEndLines.get(elementStartLine));
      }
    }
    return coveredLines;
  }

  @Nullable
  private Integer nextCodeLine(int noSonarLine) {
    if (codeLines.contains(noSonarLine)) {
      return null;
    }
    return codeLines.higher(noSonarLine);
  }

  private static void addLines(Set<Integer> lines, int startLine, int endLine) {
    for (int line = startLine; line <= endLine; line++) {
      lines.add(line);
    }
  }
}
