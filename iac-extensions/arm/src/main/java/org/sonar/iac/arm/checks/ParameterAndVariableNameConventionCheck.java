/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.checks;

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

@Rule(key = "S117")
public class ParameterAndVariableNameConventionCheck implements IacCheck {

  private static final String MESSAGE = "Rename this %s \"%s\" to match the regular expression '%s'.";
  private static final String DEFAULT_FORMAT = "^[a-z][a-zA-Z0-9]*$";

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the names of variables and parameters against.",
    defaultValue = DEFAULT_FORMAT)
  public String format = DEFAULT_FORMAT;

  private Pattern pattern;

  @Override
  public void initialize(InitContext init) {
    pattern = Pattern.compile(format);
    init.register(ParameterDeclaration.class, this::check);
    init.register(VariableDeclaration.class, this::check);
  }

  private void check(CheckContext checkContext, Declaration declaration) {
    var name = declaration.declaratedName().value();
    var matcher = pattern.matcher(name);
    if (!matcher.matches()) {
      var type = "variable";
      if (declaration.is(ArmTree.Kind.PARAMETER_DECLARATION)) {
        type = "parameter";
      }
      String message = MESSAGE.formatted(type, name, format);
      checkContext.reportIssue(declaration.declaratedName(), message);
    }
  }
}
