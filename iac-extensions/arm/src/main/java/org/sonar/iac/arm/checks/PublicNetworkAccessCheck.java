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

import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;

@Rule(key = "S6329")
public class PublicNetworkAccessCheck extends AbstractArmResourceCheck {

  private static final Set<String> SENSITIVE_VALUES = Set.of("Enabled", "EnabledForSessionHostsOnly", "EnabledForClientsOnly");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.DesktopVirtualization/hostPools",
      (ctx, resource) -> {
        Optional<PropertyTree> tree = PropertyUtils.get(resource, "publicNetworkAccess");
        if (tree.isPresent() && (tree.get().value() instanceof StringLiteral)) {
          StringLiteral literal = (StringLiteral) tree.get().value();
          if (SENSITIVE_VALUES.contains(literal.value())) {
            ctx.reportIssue(tree.get(), "Make sure allowing public network access is safe here.");
          }
        }
      });
  }
}
