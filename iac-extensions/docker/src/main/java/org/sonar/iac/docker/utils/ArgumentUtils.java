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

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
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
    for (Argument argument : hasArguments.arguments()) {
      sb.append(resolve(argument).value);
    }
    return new ArgumentResolution(sb.toString());
  }

  public static ArgumentResolution resolve(Argument argument) {
    return resolve(argument.expressions());
  }

  public static ArgumentResolution resolve(List<Expression> expressions) {
    StringBuilder sb = new StringBuilder();
    for (Expression expression : expressions) {
      String expressionResolution = resolveExpression(expression);
      if (expressionResolution == null) {
        return new ArgumentResolution(null);
      }
      sb.append(expressionResolution);
    }
    return new ArgumentResolution(sb.toString());
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

  // TODO Consider to remove by SONARIAC-579 Remove LiteralList
  @Nullable
  public static SyntaxToken argumentToSyntaxToken(Argument argument) {
    String value = ArgumentUtils.resolve(argument).value();
    if (value != null) {
      return new SyntaxTokenImpl(value, argument.textRange(), Collections.emptyList());
    }
    return null;
  }

  public static class ArgumentResolution {

    private final String value;

    ArgumentResolution(@Nullable String value) {
      this.value = value;
    }

    @Nullable
    public String value() {
      return value;
    }
  }
}
