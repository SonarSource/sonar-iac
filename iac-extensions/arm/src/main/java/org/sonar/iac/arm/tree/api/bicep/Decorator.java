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
package org.sonar.iac.arm.tree.api.bicep;

import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;

public interface Decorator extends HasKeyword {

  Expression expression();

  @CheckForNull
  default FunctionCall functionCallOrMemberFunctionCall() {
    Expression decoratorExpression = expression();
    if (decoratorExpression instanceof FunctionCall functionCall) {
      return functionCall;
    } else if (decoratorExpression instanceof MemberExpression memberExpression && memberExpression.expression() instanceof FunctionCall functionCall) {
      return functionCall;
    } else {
      return null;
    }
  }
}
