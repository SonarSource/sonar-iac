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
  protected void registerResourceChecks() {
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
