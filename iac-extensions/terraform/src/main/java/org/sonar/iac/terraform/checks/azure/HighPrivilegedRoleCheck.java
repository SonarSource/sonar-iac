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
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

import java.util.Set;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

@Rule(key = "S6381")
public class HighPrivilegedRoleCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that assigning the %s role is safe here.";

  private static final Set<String> HIGH_PRIVILEGED_ROLES = Set.of("Owner", "Contributor", "User Access Administrator");

  @Override
  protected void registerChecks() {
    register(HighPrivilegedRoleCheck::checkRoleDefinitionName, "azurerm_role_assignment");
  }

  private static void checkRoleDefinitionName(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "role_definition_name", AttributeTree.class)
      .filter(attr -> TextUtils.matchesValue(attr.value(), HIGH_PRIVILEGED_ROLES::contains).isTrue())
      .ifPresent(attr -> ctx.reportIssue(attr, String.format(MESSAGE, ((TextTree) attr.value()).value())));
  }
}

