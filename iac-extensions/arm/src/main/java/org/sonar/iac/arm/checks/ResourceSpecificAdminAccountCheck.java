/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
