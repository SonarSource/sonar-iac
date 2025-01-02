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
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isTrue;

@Rule(key = "S6379")
public class ResourceSpecificAdminAccountCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Make sure that enabling an administrative account or administrative permissions is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Batch/batchAccounts/pools", ResourceSpecificAdminAccountCheck::checkBatchAccountsPools);
    register("Microsoft.ContainerRegistry/registries", ResourceSpecificAdminAccountCheck::checkContainerRegistryRegistries);
  }

  private static void checkBatchAccountsPools(ContextualResource contextualResource) {
    contextualResource.objectsByPath("startTask/userIdentity/autoUser")
      .forEach(autoUser -> autoUser.property("elevationLevel")
        .reportIf(expression -> TextUtils.isValue(expression, "Admin").isTrue(), MESSAGE));
  }

  private static void checkContainerRegistryRegistries(ContextualResource contextualResource) {
    contextualResource.property("adminUserEnabled")
      .reportIf(isTrue(), MESSAGE);
  }
}
