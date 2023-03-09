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
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.Variable;
import org.sonarsource.analyzer.commons.collections.ListUtils;

import static org.sonar.iac.docker.utils.ArgumentUtils.ArgumentResolution.Status.RESOLVED;
import static org.sonar.iac.docker.utils.ArgumentUtils.ArgumentResolution.Status.UNRESOLVED;

public class ArgumentUtils {

  private ArgumentUtils() {
    // utils class
  }

  public static ArgumentResolution resolve(@Nullable Argument argument) {
    return ArgumentResolver.resolve(argument);
  }

  private static class ArgumentResolver {

    ArgumentResolution.Builder resolution = new ArgumentResolution.Builder();
    Set<Variable> visitedVariable = new HashSet<>();

    private static ArgumentResolution resolve(@Nullable Argument argument) {
      return new ArgumentResolver().resolveArgument(argument);
    }

    private ArgumentResolution resolveArgument(@Nullable Argument argument) {
      if (argument == null) {
        return ArgumentResolution.EMPTY;
      }
      resolveExpressions(argument.expressions());
      return resolution.build();
    }

    private void resolveExpressions(List<Expression> expressions) {
      for (Expression expression : expressions) {
        resolveExpression(expression);
      }
    }

    private void resolveExpression(Expression expression) {
      switch (expression.getKind()) {
        case STRING_LITERAL:
          resolution.addValue(((Literal)expression).value());
          break;
        case EXPANDABLE_STRING_CHARACTERS:
          resolution.addValue(((ExpandableStringCharacters)expression).value());
          break;
        case EXPANDABLE_STRING_LITERAL:
          resolveExpressions(((ExpandableStringLiteral)expression).expressions());
          break;
        case REGULAR_VARIABLE:
          resolveVariable((Variable) expression);
          break;
        case ENCAPSULATED_VARIABLE:
          EncapsulatedVariable encapsulatedVariable = (EncapsulatedVariable) expression;
          if (!":+".equals((encapsulatedVariable).modifierSeparator())) {
            resolveVariable(encapsulatedVariable);
          } else {
            resolution.setUnresolved();
          }
          break;
        default:
      }
    }

    /**
     * To resolve the value of a symbol at a given state, the last assigned value is considered.
     * There for all symbol usages are analyzed for the last assignment with value before the access.
     */
    private void resolveVariable(Variable variable) {
      Symbol symbol = variable.symbol();
      if (!visitedVariable.add(variable) || symbol == null) {
        resolution.setUnresolved();
        return;
      }

      List<Usage> usages = ListUtils.reverse(symbol.usages());
      List<Usage> reversedAssignments = usages.stream()
        .filter(usage -> usage.kind().equals(Usage.Kind.ASSIGNMENT))
        .collect(Collectors.toList());
      Scope.Kind accessScopeKind = usages.get(0).scope().kind();

      Argument lastAssignedValue = findLastAccessibleAssignedValue(reversedAssignments, accessScopeKind);
      if (lastAssignedValue != null) {
        resolveExpressions(lastAssignedValue.expressions());
      } else {
        resolution.setUnresolved();
      }
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

    public enum Status {
      RESOLVED,
      UNRESOLVED,
      EMPTY
    }

    static final ArgumentResolution EMPTY = new ArgumentResolution("", Status.EMPTY);

    private final String value;
    private final Status status;

    private ArgumentResolution(String value, Status status) {
      this.value = value;
      this.status = status;
    }

    public String value() {
      return value;
    }

    public Status status() {
      return status;
    }

    public boolean is(Status status) {
      return this.status == status;
    }

    private static class Builder {

      private Status status = RESOLVED;
      private final StringBuilder sb = new StringBuilder();

      private void addValue(String value) {
        sb.append(value);
      }

      private void setUnresolved() {
        status = UNRESOLVED;
      }

      public ArgumentResolution build() {
        return new ArgumentResolution(sb.toString(), status);
      }
    }
  }
}
