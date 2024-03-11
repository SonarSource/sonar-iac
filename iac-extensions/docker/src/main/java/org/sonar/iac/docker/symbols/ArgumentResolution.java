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
package org.sonar.iac.docker.symbols;

import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EncapsulatedVariable;
import org.sonar.iac.docker.tree.api.ExpandableStringCharacters;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.Variable;
import org.sonarsource.analyzer.commons.collections.ListUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class dedicated to resolving {@link Argument}, which is a complex object used to represent most parameters of instruction.
 */
public class ArgumentResolution {

  public enum Status {
    RESOLVED,
    UNRESOLVED,
    EMPTY
  }

  static final ArgumentResolution EMPTY = new ArgumentResolution(null, "", Status.EMPTY) {
    @Override
    public Argument argument() {
      throw new IllegalStateException("The root argument should not be requested from an empty resolution");
    }
  };

  private final String value;
  private final Status status;

  @Nullable
  private final Argument argument;

  public ArgumentResolution(@Nullable Argument argument, String value, Status status) {
    this.argument = argument;
    this.value = value;
    this.status = status;
  }

  /**
   * Main method of the class, the point of entry to resolve {@link Argument} in order to exploit the result in checks.
   * In docker, most instruction can expect one or even multiple arguments, which can be a mix of strings, quotes string with spaces,
   * variable reference with or without modifier, ect.
   * This method will provide an ArgumentResolution object with the result of the tentative of resolution: the status and the String value.
   * The quotes and double quotes in string literals are striped by default.
   */
  public static ArgumentResolution of(@Nullable Argument argument) {
    return ArgumentResolver.resolve(argument, true);
  }

  /**
   * The method is similar to {@code ArgumentResolution#of} but there is a control of strip quotes or double quotes in string literal.
   */
  public static ArgumentResolution ofWithoutStrippingQuotes(@Nullable Argument argument) {
    return ArgumentResolver.resolve(argument, false);
  }

  public String value() {
    return value;
  }

  public Status status() {
    return status;
  }

  public Argument argument() {
    return argument;
  }

  public boolean isResolved() {
    return this.status == Status.RESOLVED;
  }

  public boolean isUnresolved() {
    return this.status == Status.UNRESOLVED;
  }

  public boolean isEmpty() {
    return this.status == Status.EMPTY;
  }

  private static class Builder {

    private final Argument argument;
    private Status status = Status.RESOLVED;
    private final StringBuilder sb = new StringBuilder();

    public Builder(Argument argument) {
      this.argument = argument;
    }

    private void addValue(String value) {
      sb.append(value);
    }

    private void setUnresolved() {
      status = Status.UNRESOLVED;
    }

    public ArgumentResolution build() {
      return new ArgumentResolution(argument, sb.toString(), status);
    }
  }

  private static class ArgumentResolver {

    private final boolean stripQuotes;
    Builder builder;
    Set<Variable> visitedVariable = new HashSet<>();

    private ArgumentResolver(boolean stripQuotes) {
      this.stripQuotes = stripQuotes;
    }

    private static ArgumentResolution resolve(@Nullable Argument argument, boolean stripQuotes) {
      return new ArgumentResolver(stripQuotes).resolveArgument(argument);
    }

    private ArgumentResolution resolveArgument(@Nullable Argument argument) {
      if (argument == null) {
        return ArgumentResolution.EMPTY;
      }
      builder = new Builder(argument);
      resolveExpressions(argument.expressions());
      return builder.build();
    }

    private void resolveExpressions(List<Expression> expressions) {
      for (Expression expression : expressions) {
        resolveExpression(expression);
      }
    }

    private void resolveExpression(Expression expression) {
      switch (expression.getKind()) {
        case STRING_LITERAL:
          if (shouldKeepQuotes(expression)) {
            builder.addValue(((Literal) expression).originalValue());
          } else {
            builder.addValue(((Literal) expression).value());
          }
          break;
        case EXPANDABLE_STRING_CHARACTERS:
          builder.addValue(((ExpandableStringCharacters) expression).value());
          break;
        case EXPANDABLE_STRING_LITERAL:
          maybeAddQuote(expression, ((ExpandableStringLiteral) expression).getOpenDoubleQuote());
          resolveExpressions(((ExpandableStringLiteral) expression).expressions());
          maybeAddQuote(expression, ((ExpandableStringLiteral) expression).getCloseDoubleQuote());
          break;
        case REGULAR_VARIABLE:
          resolveVariable((Variable) expression);
          break;
        case ENCAPSULATED_VARIABLE:
          EncapsulatedVariable encapsulatedVariable = (EncapsulatedVariable) expression;
          if (!":+".equals((encapsulatedVariable).modifierSeparator())) {
            resolveVariable(encapsulatedVariable);
          } else {
            builder.setUnresolved();
          }
          break;
        default:
          builder.setUnresolved();
      }
    }

    private boolean shouldKeepQuotes(Expression expression) {
      return !stripQuotes && expression.parent() != null && expression.parent().parent().is(DockerTree.Kind.SHELL_FORM);
    }

    private void maybeAddQuote(Expression expression, SyntaxToken quote) {
      if (shouldKeepQuotes(expression)) {
        builder.addValue(quote.value());
      }
    }

    /**
     * To resolve the value of a symbol at a given state, the last assigned value is considered.
     * There for all symbol usages are analyzed for the last assignment with value before the access.
     */
    private void resolveVariable(Variable variable) {
      Symbol symbol = variable.symbol();
      if (!visitedVariable.add(variable) || symbol == null) {
        builder.setUnresolved();
        return;
      }

      List<Usage> usages = ListUtils.reverse(symbol.usages());
      List<Usage> reversedAssignments = usages.stream()
        .filter(usage -> usage.kind().equals(Usage.Kind.ASSIGNMENT))
        .toList();
      Scope.Kind accessScopeKind = usages.get(0).scope().kind();

      Argument lastAssignedValue = findLastAccessibleAssignedValue(reversedAssignments, accessScopeKind);
      if (lastAssignedValue != null) {
        resolveExpressions(lastAssignedValue.expressions());
      } else {
        builder.setUnresolved();
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
}
