/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.springconfig.parser.yaml;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringConfigYamlPreprocessor {
  private static final String DOCUMENT_BREAK = "---";
  private static final int DOCUMENT_BREAK_LENGTH = DOCUMENT_BREAK.length();

  public String preprocess(String source) {
    return Stream.of(source.split("\n", -1))
      .map(SpringConfigYamlPreprocessor::removeInlineCommentAfterDocumentBreak)
      .collect(Collectors.joining("\n"));
  }

  private static String removeInlineCommentAfterDocumentBreak(String line) {
    var trimmed = line.trim();
    if (trimmed.startsWith(DOCUMENT_BREAK)) {
      var textAfterBreak = trimmed.substring(DOCUMENT_BREAK_LENGTH).trim();
      if (textAfterBreak.startsWith("#")) {
        return line.substring(0, line.indexOf(DOCUMENT_BREAK) + DOCUMENT_BREAK_LENGTH);
      }
    }
    return line;
  }
}
