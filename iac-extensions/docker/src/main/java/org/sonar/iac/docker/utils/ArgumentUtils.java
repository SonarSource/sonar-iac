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
package org.sonar.iac.docker.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.symbols.Symbol;
import org.sonar.iac.docker.symbols.Usage;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EncapsulatedVariable;
import org.sonar.iac.docker.tree.api.ExpandableStringCharacters;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.HasArguments;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.Variable;

public class ArgumentUtils {

  private ArgumentUtils() {
    // utils class
  }

  /**
   * Resolve a list of Argument as a single string, used by some instructions like USER.
   * Will return an empty {@link ArgumentResolution} when it is not possible to resolve the argument expressions.
   */
  public static ArgumentResolution resolveAndMerge(HasArguments hasArguments) {
    StringBuilder sb = new StringBuilder();
    for (Argument argument : hasArguments.arguments()) {
      ArgumentResolution resolved = resolve(argument);
      sb.append(resolved.value);
    }
    return new ArgumentResolution(sb.toString());
  }

  public static ArgumentResolution resolve(Argument argument) {
    return ArgumentResolver.resolve(argument);
  }

  private static class ArgumentResolver {

    Set<Expression> visitedExpressions = new HashSet<>();

    private static ArgumentResolution resolve(Argument argument) {
      return new ArgumentResolver().resolveExpressions(argument.expressions());
    }

    private ArgumentResolution resolveExpressions(List<Expression> expressions) {
      StringBuilder sb = new StringBuilder();
      for (Expression expression : expressions) {
        if (!visitedExpressions.add(expression)) {
          return ArgumentResolution.EMPTY;
        }

        String expressionResolution = resolveExpression(expression);
        if (expressionResolution == null) {
          return ArgumentResolution.EMPTY;
        }
        sb.append(expressionResolution);
      }
      return new ArgumentResolution(sb.toString());
    }

    @Nullable
    private String resolveExpression(Expression expression) {
      if (expression.is(DockerTree.Kind.STRING_LITERAL)) {
        return ((Literal)expression).value();
      }
      if (expression.is(DockerTree.Kind.EXPANDABLE_STRING_LITERAL)) {
        return resolveExpressions(((ExpandableStringLiteral)expression).expressions()).value();
      }
      if (expression.is(DockerTree.Kind.EXPANDABLE_STRING_CHARACTERS)) {
        return ((ExpandableStringCharacters)expression).value();
      }
      if (expression.is(DockerTree.Kind.REGULAR_VARIABLE)
        || (expression.is(DockerTree.Kind.ENCAPSULATED_VARIABLE) && !":+".equals(((EncapsulatedVariable) expression).modifierSeparator()))) {
        return resolveVariable((Variable) expression).value();
      }
      return null;
    }

    private ArgumentResolution resolveVariable(Variable variable) {
      Symbol symbol = variable.symbol();
      return symbol != null ? resolveSymbol(symbol) : ArgumentResolution.EMPTY;
    }

    /**
     * To resolve the value of a symbol at a given state, the last assigned value is considered.
     * There for all symbol usages are analyzed for the last assignment with value before the access.
     */
    private ArgumentResolution resolveSymbol(Symbol symbol) {
      List<Usage> usages = symbol.usages();
      Collections.reverse(usages);

      List<Usage> reversedAssignments = usages.stream()
        .filter(usage -> usage.kind().equals(Usage.Kind.ASSIGNMENT))
        .collect(Collectors.toList());
      Scope.Kind accessScopeKind = usages.get(0).scope().kind();

      Argument lastAssignedValue = findLastAccessibleAssignedValue(reversedAssignments, accessScopeKind);

      return lastAssignedValue != null ? resolveExpressions(lastAssignedValue.expressions()) : ArgumentResolution.EMPTY;
    }

    /**
     * In Dockerfiles exit two kinds of scopes where variables can be defined and accessed.
     * To access a variable from the global scope inside a DockerImage scope the variable access has to be enabled
     * by an assignment instruction of the variable without value.
     */
    @Nullable
    private static Argument findLastAccessibleAssignedValue(List<Usage> assignments, Scope.Kind accessScopeKind) {
      boolean hasAccessToGlobalScope = false;
      for (Usage assignment : assignments) {
        if (assignment.tree().is(DockerTree.Kind.KEY_VALUE_PAIR)) {
          KeyValuePair assignmentTree = (KeyValuePair) assignment.tree();
          Argument value = assignmentTree.value();
          if (value != null) {
            return (assignment.scope().kind().equals(accessScopeKind) || hasAccessToGlobalScope) ? value : null;
          } else {
            hasAccessToGlobalScope = true;
          }
        }
      }
      return null;
    }
  }

  public static class ArgumentResolution {

    static final ArgumentResolution EMPTY = new ArgumentResolution(null);

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
