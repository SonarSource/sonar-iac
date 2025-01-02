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

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.checks.utils.CheckUtils;
import org.sonar.iac.arm.tree.api.Expression;

@Rule(key = "S4507")
public class DebugSettingCheck extends AbstractArmResourceCheck {
  private static final String MESSAGE = "Make sure this debug feature is deactivated before delivering the code in production.";
  private static final Predicate<Expression> SENSITIVE_DETAIL_LEVEL = detailLevel -> CheckUtils.contains("RequestContent").or(CheckUtils.contains("ResponseContent"))
    .test(detailLevel);

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Resources/deployments", checkDebugSetting());
  }

  private static Consumer<ContextualResource> checkDebugSetting() {
    return (ContextualResource resource) -> {
      if (resource.object("debugSetting").property("detailLevel").is(SENSITIVE_DETAIL_LEVEL)) {
        resource.property("debugSetting").report(MESSAGE);
      }
    };
  }
}
