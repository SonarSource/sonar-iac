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

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringConfigYamlPreprocessor {
  private static final Pattern LINE_SEPARATORS = Pattern.compile("(\n|\r|\r\n|\u2028|\u2029)");
  private static final String DOCUMENT_BREAK = "---";
  private static final int DOCUMENT_BREAK_LENGTH = DOCUMENT_BREAK.length();
  private static final Pattern MAVEN_VARIABLE_AT_TOKEN_START = Pattern.compile("([^:]++:\\p{javaWhitespace}*)@(?<name>[^@]++)@(.*+)");
  private static final Predicate<String> containsInlineCommentAfterDocumentBreak = line -> line.startsWith(DOCUMENT_BREAK);
  private static final Predicate<String> containsMavenVariableAtTokenStart = line -> MAVEN_VARIABLE_AT_TOKEN_START.matcher(line).matches();

  public String preprocess(String source) {
    var lines = LINE_SEPARATORS.split(source, -1);

    if (Stream.of(lines).noneMatch(containsInlineCommentAfterDocumentBreak.or(containsMavenVariableAtTokenStart))) {
      return source;
    }

    return Stream.of(lines)
      .map(SpringConfigYamlPreprocessor::removeInlineCommentAfterDocumentBreak)
      .map(SpringConfigYamlPreprocessor::transformMavenSubstitutions)
      .collect(Collectors.joining("\n"));
  }

  private static String removeInlineCommentAfterDocumentBreak(String line) {
    if (line.startsWith(DOCUMENT_BREAK)) {
      var textAfterBreak = line.substring(DOCUMENT_BREAK_LENGTH).trim();
      if (textAfterBreak.startsWith("#")) {
        return line.substring(0, line.indexOf(DOCUMENT_BREAK) + DOCUMENT_BREAK_LENGTH);
      }
    }
    return line;
  }

  /**
   * Surround Maven variable substitution in quotes instead of '@' to avoid parsing issues.
   * We don't resolve these variables, so we shouldn't lose information by doing this.
   */
  private static String transformMavenSubstitutions(String line) {
    return MAVEN_VARIABLE_AT_TOKEN_START.matcher(line).replaceAll("$1'${name}'$3");
  }
}
