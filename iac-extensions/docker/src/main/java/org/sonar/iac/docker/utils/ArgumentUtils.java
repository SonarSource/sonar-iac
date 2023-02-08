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

import javax.annotation.Nullable;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.Literal;

public class ArgumentUtils {

  private ArgumentUtils() {
    // utils class
  }

  public static ArgumentResolution resolve(Argument argument) {
    StringBuilder sb = new StringBuilder();
    for (Expression expression : argument.expressions()) {
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
    return null;
  }

  static class ArgumentResolution {

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
