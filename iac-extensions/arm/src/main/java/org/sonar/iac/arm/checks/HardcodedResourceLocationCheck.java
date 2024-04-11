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

import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
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
    ArmTreeUtils.getResourceProperty(resource, "location")
      .map(Property::value)
      .filter(tree -> !(tree instanceof HasIdentifier))
      .ifPresent(tree -> ctx.reportIssue(tree.textRange(), MESSAGE));
  }
}
