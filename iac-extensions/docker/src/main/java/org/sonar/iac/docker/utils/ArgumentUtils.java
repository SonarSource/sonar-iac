/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExpandableStringCharacters;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.HasArguments;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.impl.SyntaxTokenImpl;

public class ArgumentUtils {

  private ArgumentUtils() {
    // utils class
  }

  /**
   * Resolve a list of Argument as a single string, used by some instructions like USER.
   */
  public static ArgumentResolution resolveAndMerge(HasArguments hasArguments) {
    StringBuilder sb = new StringBuilder();
    List<TextRange> ranges = new ArrayList<>();
    for (Argument argument : hasArguments.arguments()) {
      ArgumentResolution resolved = resolve(argument);
      sb.append(resolved.value);
      ranges.add(resolved.textRange);
    }
    return new ArgumentResolution(sb.toString(), TextRanges.merge(ranges));
  }

  public static ArgumentResolution resolve(Argument argument) {
    return resolve(argument.expressions());
  }

  public static ArgumentResolution resolve(List<Expression> expressions) {
    StringBuilder sb = new StringBuilder();
    List<TextRange> ranges = new ArrayList<>();
    for (Expression expression : expressions) {
      String expressionResolution = resolveExpression(expression);
      if (expressionResolution == null) {
        return new ArgumentResolution(null, null);
      }
      sb.append(expressionResolution);
      ranges.add(expression.textRange());
    }
    return new ArgumentResolution(sb.toString(), TextRanges.merge(ranges));
  }

  @Nullable
  private static String resolveExpression(Expression expression) {
    if (expression.is(DockerTree.Kind.STRING_LITERAL)) {
      return ((Literal)expression).value();
    }
    if (expression.is(DockerTree.Kind.EXPANDABLE_STRING_LITERAL)) {
      return resolve(((ExpandableStringLiteral)expression).expressions()).value();
    }
    if (expression.is(DockerTree.Kind.EXPANDABLE_STRING_CHARACTERS)) {
      return ((ExpandableStringCharacters)expression).value();
    }
    return null;
  }

  @Nullable
  public static SyntaxToken argumentToSyntaxToken(Argument argument) {
    return ArgumentUtils.resolve(argument).asSyntaxToken();
  }

  public static List<SyntaxToken> argumentsToSyntaxTokens(List<Argument> arguments) {
    return arguments.stream()
      .map(ArgumentUtils::argumentToSyntaxToken)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  public static class ArgumentResolution {

    private final String value;
    private final TextRange textRange;

    ArgumentResolution(@Nullable String value, @Nullable TextRange textRange) {
      this.value = value;
      this.textRange = textRange;
    }

    @Nullable
    public String value() {
      return value;
    }

    @Nullable
    public TextRange textRange() {
      return textRange;
    }

    @Nullable
    public SyntaxToken asSyntaxToken() {
      if (value == null || textRange == null) {
        return null;
      }
      return new SyntaxTokenImpl(value, textRange, Collections.emptyList());
    }
  }
}
