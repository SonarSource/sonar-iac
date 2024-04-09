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
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.checks.utils.CheckUtils;
import org.sonar.iac.arm.tree.api.Expression;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Rule(key = "S4507")
public class DebugSettingCheck extends AbstractArmResourceCheck {
  private static final String MESSAGE = "Make sure this debug feature is deactivated before delivering the code in production.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Resources/deployments", checkDebugSetting());
  }

  private static Consumer<ContextualResource> checkDebugSetting() {
    return (ContextualResource resource) -> {
      if (resource.object("debugSetting").property("detailLevel").is(sensitiveDetailLevel())) {
        resource.property("debugSetting").report(MESSAGE);
      }
    };
  }

  private static Predicate<Expression> sensitiveDetailLevel() {
    return detailLevel -> CheckUtils.contains("RequestContent").or(CheckUtils.contains("ResponseContent")).test(detailLevel);
  }
}
