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
package org.sonar.iac.terraform.checks.utils;

import java.util.function.Predicate;
import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.terraform.api.tree.ExpressionTree;

import static org.sonar.iac.terraform.api.tree.TerraformTree.Kind.ATTRIBUTE_ACCESS;

public class TerraformUtils {

  private TerraformUtils() {
    // utils class
  }

  public static Trilean attributeAccessMatches(ExpressionTree expression, Predicate<String> predicate) {
    if (expression.is(ATTRIBUTE_ACCESS)) {
      return predicate.test(expression.toString()) ? Trilean.TRUE : Trilean.FALSE;
    }
    return Trilean.UNKNOWN;
  }
}
