/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
   * Extract resource name from Terraform expression. Resource names are in a form {@code resource_type.resource_name},
   * for example {@code aws_s3_bucket.example}. This method extracts the {@code example} part from such expressions.
   * <p>
   */
  public static Optional<String> getResourceName(ExpressionTree expression) {
    if (expression instanceof AttributeAccessTree attributeAccess) {
      // Using resource identifier directly, like aws_s3_bucket.example; we need to capture the `example` part
      return Optional.of(attributeAccess.attribute().value());
    } else if (expression instanceof IndexAccessExprTree indexAccess && indexAccess.subject() instanceof AttributeAccessTree subject) {
      // Expression is like aws_s3_bucket.example[X] when `count` or `for_each` meta-arg is used.
      // X can be number, `count.index`, `each.key`, etc. Again, we need to capture the `example` part.
      return Optional.of(subject.attribute().value());
    }
    return Optional.empty();
  }
}
