/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.checks.azure;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

@Rule(key = "S6379")
public class ResourceSpecificAdminAccountCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that enabling an administrative account or administrative permissions is safe here.";

  @Override
  protected void registerChecks() {
    register(ResourceSpecificAdminAccountCheck::checkContainerRegistry, "azurerm_container_registry");
    register(ResourceSpecificAdminAccountCheck::checkBatchPool, "azurerm_batch_pool");
  }

  private static void checkContainerRegistry(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "admin_enabled", AttributeTree.class)
      .filter(attr -> TextUtils.isValueTrue(attr.value()))
      .ifPresent(attr -> ctx.reportIssue(attr, MESSAGE));
  }

  private static void checkBatchPool(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "start_task", BlockTree.class)
      .flatMap(startTask -> PropertyUtils.get(startTask, "user_identity", BlockTree.class))
      .flatMap(userIdentity -> PropertyUtils.get(userIdentity, "auto_user", BlockTree.class))
      .flatMap(autoUser -> PropertyUtils.get(autoUser, "elevation_level", AttributeTree.class))
      .filter(attr -> TextUtils.isValue(attr.value(), "Admin").isTrue())
      .ifPresent(attr -> ctx.reportIssue(attr, MESSAGE));
  }

}
