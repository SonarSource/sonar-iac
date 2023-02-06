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
package org.sonar.iac.docker.tree.impl;

import java.util.List;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExpandableStringCharacters;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.Literal;

public class ExecFormUtils {

  public static String toString(Argument arg) {
    return toString(arg.expressions());
  }

  public static String toString(List<Expression> expressions) {
    StringBuilder sb = new StringBuilder();
    for (Expression expr : expressions) {
      sb.append(toString(expr));
    }
    return sb.toString();
  }

  public static String toString(Expression expr) {
    if (expr.is(DockerTree.Kind.STRING_LITERAL)) {
      return ((Literal)expr).value();
    }
    if (expr.is(DockerTree.Kind.EXPANDABLE_STRING_LITERAL)) {
      return toString(((ExpandableStringLiteral)expr).expressions());
    }
    if (expr.is(DockerTree.Kind.EXPANDABLE_STRING_CHARACTERS)) {
      return ((ExpandableStringCharacters)expr).value();
    }
    throw new RuntimeException("Unexpected class for convert(Expression) : " + expr.getClass());
  }
}
