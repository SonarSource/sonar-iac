/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

@Rule(key = "S6381")
public class HighPrivilegedRoleCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that assigning the %s role is safe here.";

  private static final Set<String> HIGH_PRIVILEGED_ROLES = Set.of("Owner", "Contributor", "User Access Administrator");

  @Override
  protected void registerResourceChecks() {
    register(HighPrivilegedRoleCheck::checkRoleDefinitionName, "azurerm_role_assignment");
  }

  private static void checkRoleDefinitionName(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "role_definition_name", AttributeTree.class)
      .filter(attr -> TextUtils.matchesValue(attr.value(), HIGH_PRIVILEGED_ROLES::contains).isTrue())
      .ifPresent(attr -> ctx.reportIssue(attr, String.format(MESSAGE, ((TextTree) attr.value()).value())));
  }
}
