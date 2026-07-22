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
package org.sonar.iac.terraform.checks.utils;

import java.util.Optional;
import java.util.function.Predicate;
import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.IndexAccessExprTree;
import org.sonar.iac.terraform.api.tree.IndexSplatAccessTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;

import static org.sonar.iac.terraform.api.tree.TerraformTree.Kind.ATTRIBUTE_ACCESS;

public class TerraformUtils {

  private TerraformUtils() {
    // utils class
  }

  public static Trilean attributeAccessMatches(ExpressionTree expression, Predicate<String> predicate) {
    if (expression.is(ATTRIBUTE_ACCESS)) {
      return predicate.test(attributeAccessToString((AttributeAccessTree) expression)) ? Trilean.TRUE : Trilean.FALSE;
    }
    return Trilean.UNKNOWN;
  }

  public static String attributeAccessToString(AttributeAccessTree attributeAccess) {
    StringBuilder sb = new StringBuilder();
    ExpressionTree object = attributeAccess.object();
    if (object instanceof AttributeAccessTree attributeAccessTree) {
      sb.append(attributeAccessToString(attributeAccessTree));
      sb.append('.');
    } else if (object instanceof VariableExprTree variableExprTree) {
      sb.append(variableExprTree.value());
      sb.append('.');
    }
    sb.append(attributeAccess.attribute().value());
    return sb.toString();
  }

  /**
   * Extract the resource name from a reference like {@code aws_s3_bucket.example} (returns {@code example}).
   * A trailing index/splat from {@code count}/{@code for_each} ({@code aws_s3_bucket.example[0]}) is unwrapped first.
   */
  public static Optional<String> getResourceName(ExpressionTree expression) {
    if (unwrapIndexAccess(expression) instanceof AttributeAccessTree attributeAccess) {
      return Optional.of(attributeAccess.attribute().value());
    }
    return Optional.empty();
  }

  /**
   * Discard a trailing index or splat access ({@code x[0]}, {@code x["k"]}, {@code x[*]}) and return the wrapped subject.
   * Such indexing appears when the referenced resource uses {@code count} or {@code for_each}.
   */
  public static ExpressionTree unwrapIndexAccess(ExpressionTree expression) {
    if (expression instanceof IndexAccessExprTree indexAccess) {
      return indexAccess.subject();
    }
    if (expression instanceof IndexSplatAccessTree splatAccess) {
      return splatAccess.subject();
    }
    return expression;
  }
}
