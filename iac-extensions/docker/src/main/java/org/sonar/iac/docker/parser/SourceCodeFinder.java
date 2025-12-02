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

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextPointer;

public class SourceCodeFinder {

  private static final Pattern DOCKERFILE_HEREDOC_PATTERN = Pattern
    .compile("^(?<leading>\\s*+)(?<openMarker><<-?)(?<tagName>\\w++)(?<content>[\\s\\S]*)(?<closeTag>\\k<tagName>)$");

  private String source = "";
  // Visible for testing
  List<String> cachedSourceLines = null;
  private String lineSeparator;

  // Allow to set the current source file being parsed, used to retrieve the original non-preprocessed shell code to parse
  public void setSource(String source) {
    this.source = source;
    this.cachedSourceLines = null;
  }

  public String findSourceCode(HasTextRange node) {
    return findSourceCode(node.textRange().start(), node.textRange().end());
  }

  public String findSourceCode(TextPointer from, TextPointer to) {
    var lines = getSourceLines();
    var startLine = from.line() - 1;
    var startLineOffset = from.lineOffset();
    var endLine = to.line() - 1;
    var endLineOffset = to.lineOffset();

    var nodeSource = IntStream.rangeClosed(startLine, endLine)
      .mapToObj(i -> {
        if (i >= lines.size() && lines.get(lines.size() - 1).stripTrailing().endsWith("\\")) {
          return "";
        }
        var line = lines.get(i);
        int start = (i == startLine) ? startLineOffset : 0;
        int end = (i == endLine) ? endLineOffset : line.length();
        return line.substring(start, end);
      })
      .collect(Collectors.joining(lineSeparator));
    return cleanupDockerfileHeredocIfPresent(nodeSource);
  }

  public String lineSeparator() {
    return lineSeparator;
  }

  private List<String> getSourceLines() {
    if (cachedSourceLines == null) {
      lineSeparator = computeLineSeparator(source);
      cachedSourceLines = source.lines().toList();
    }
    return cachedSourceLines;
  }

  private static String computeLineSeparator(String source) {
    if (source.isEmpty()) {
      return "\n";
    } else if (source.contains("\r\n")) {
      return "\r\n";
    } else if (source.contains("\r")) {
      return "\r";
    } else {
      return "\n";
    }
  }

  private static String cleanupDockerfileHeredocIfPresent(String code) {
    var matcher = DOCKERFILE_HEREDOC_PATTERN.matcher(code);

    if (matcher.matches()) {
      // Extract the parts using named groups
      var leading = matcher.group("leading");
      var openMarker = matcher.group("openMarker");
      var tagName = matcher.group("tagName");
      var content = matcher.group("content");

      // Logic: Replace "<<-" + "NAME" with whitespaces
      // Calculate total length of marker plus name
      var openingReplacement = " ".repeat(openMarker.length() + tagName.length());

      // Logic: Replace closing "NAME" with whitespaces
      var closingReplacement = " ".repeat(tagName.length());

      // Reconstruct the string
      code = leading +
        openingReplacement +
        content +
        closingReplacement;
    }
    return code;
  }
}
