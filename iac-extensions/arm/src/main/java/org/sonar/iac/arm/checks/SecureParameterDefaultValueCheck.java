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
package org.sonar.iac.arm.checks;

import java.util.Map;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualParameter;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isBlankString;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isEmptyObject;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isFunctionCall;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isNull;
import static org.sonar.iac.arm.tree.ArmTreeUtils.getParametersByNames;

@Rule(key = "S6648")
public class SecureParameterDefaultValueCheck implements IacCheck {

  private static final String MESSAGE = "Remove the default value from this secure %s.";
  private static final Map<ParameterType, String> SENSITIVE_TYPE_WITH_DISPLAY_TYPE = Map.of(
    ParameterType.SECURE_STRING, "string",
    ParameterType.SECURE_OBJECT, "object");

  @Override
  public void initialize(InitContext init) {
    init.register(ParameterDeclaration.class, SecureParameterDefaultValueCheck::checkParameter);
  }

  private static void checkParameter(CheckContext ctx, ParameterDeclaration parameterDeclaration) {
    ParameterType type = parameterDeclaration.type();

    if (type != null && SENSITIVE_TYPE_WITH_DISPLAY_TYPE.containsKey(type)) {
      ContextualParameter param = ContextualParameter.fromPresent(ctx, parameterDeclaration, null);
      param.reportIf(isSensitiveDefaultValue(), String.format(MESSAGE, SENSITIVE_TYPE_WITH_DISPLAY_TYPE.get(type)));
    }
  }

  private static Predicate<Expression> isSensitiveDefaultValue() {
    return isBlankString()
      .or(isEmptyObject())
      .or(isNull())
      .or(isFunctionCall("newGuid"))
      .or(isSecureParameterReference())
      .negate();
  }

  private static Predicate<Expression> isSecureParameterReference() {
    // TODO SONARIAC-1414: Bicep: Distinguish usages of variables and parameters in the AST
    return expr -> expr instanceof HasIdentifier hasIdentifier &&
      hasIdentifier.identifier() instanceof Identifier identifier &&
      isSecureParameter(identifier, identifier.value());
  }

  private static boolean isSecureParameter(ArmTree tree, String parameterName) {
    ParameterDeclaration param = getParametersByNames(tree).get(parameterName);
    return param != null && SENSITIVE_TYPE_WITH_DISPLAY_TYPE.containsKey(param.type());
  }
}
