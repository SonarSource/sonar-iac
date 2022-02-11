/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.terraform.checks.gcp;

import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.ResourceVisitor;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.sonar.iac.terraform.checks.utils.PredicateUtils.exactMatchStringPredicate;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.treePredicate;


public class HighPrivilegedRolesOnCloudIdentityGroupCheckPart extends ResourceVisitor {

  private static final String MESSAGE = "Make sure it is safe to grant full access to the resource.";

  private static final Predicate<ExpressionTree> IS_OWNER_OR_MANAGER = treePredicate(exactMatchStringPredicate("MANAGER|OWNER", Pattern.CASE_INSENSITIVE));

  @Override
  protected void registerResourceConsumer() {
    register("google_cloud_identity_group",
      resource -> resource.blocks("roles").forEach(
        block -> block.attribute("name").reportIf(IS_OWNER_OR_MANAGER, MESSAGE)));
  }
}
