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
package org.sonar.iac.jvmframeworkconfig.parser.yaml;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JvmFrameworkConfigYamlPreprocessor {
  private static final Pattern LINE_SEPARATORS = Pattern.compile("(\r\n|\n|\r|\u2028|\u2029)");
  private static final Pattern MAVEN_VARIABLE_AT_TOKEN_START = Pattern.compile("([^:]++:\\p{javaWhitespace}*)@(?<name>[^@]++)@(.*+)");
  private static final Predicate<String> containsMavenVariableAtTokenStart = line -> MAVEN_VARIABLE_AT_TOKEN_START.matcher(line).matches();

  public String preprocess(String source) {
    var lines = LINE_SEPARATORS.split(source, -1);

    if (Stream.of(lines).noneMatch(containsMavenVariableAtTokenStart)) {
      return source;
    }

    return Stream.of(lines)
      .map(JvmFrameworkConfigYamlPreprocessor::transformMavenSubstitutions)
      .collect(Collectors.joining("\n"));
  }

  /**
   * Surround Maven variable substitution in quotes instead of '@' to avoid parsing issues.
   * We don't resolve these variables, so we shouldn't lose information by doing this.
   */
  private static String transformMavenSubstitutions(String line) {
    return MAVEN_VARIABLE_AT_TOKEN_START.matcher(line).replaceAll("$1'${name}$3'");
  }
}
