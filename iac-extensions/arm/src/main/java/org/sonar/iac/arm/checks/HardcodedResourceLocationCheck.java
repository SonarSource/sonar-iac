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
package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

@Rule(key = "S6949")
public class HardcodedResourceLocationCheck implements IacCheck {
  private static final String MESSAGE = "Replace this hardcoded location with a parameter.";

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, HardcodedResourceLocationCheck::checkResourceLocation);
  }

  private static void checkResourceLocation(CheckContext ctx, ResourceDeclaration resource) {
    resource.getResourceProperty("location")
      .map(Property::value)
      .filter(HardcodedResourceLocationCheck::hasNoIdentifier)
      .filter(HardcodedResourceLocationCheck::isNotGlobalLocation)
      .ifPresent(tree -> ctx.reportIssue(tree.textRange(), MESSAGE));
  }

  private static boolean hasNoIdentifier(Expression tree) {
    return !(tree instanceof HasIdentifier);
  }

  private static boolean isNotGlobalLocation(Expression tree) {
    if (!tree.is(ArmTree.Kind.STRING_LITERAL)) {
      return true;
    }
    var stringLiteral = (StringLiteral) tree;
    return !"global".equals(stringLiteral.value());
  }
}
