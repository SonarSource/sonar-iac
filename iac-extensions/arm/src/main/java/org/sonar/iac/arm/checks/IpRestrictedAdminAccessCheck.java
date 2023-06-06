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

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6321")
public class IpRestrictedAdminAccessCheck implements IacCheck {

  private static final String MESSAGE = "Restrict IP addresses authorized to access administration services.";
  private static final String RESOURCE_TYPE = "Microsoft.Network/networkSecurityGroups/securityRules";
  private static final Set<String> SOURCE_ADDRESS_PREFIX_SENSITIVE = Set.of("*", "0.0.0.0/0", "::/0", "Internet");

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, (ctx, resource) -> {
      if (RESOURCE_TYPE.equals(resource.type().value())) {
        PropertyUtils.get(resource, "sourceAddressPrefix").ifPresent(propertyTree -> {
          if (isSensitiveString(propertyTree.value()) || isSensitiveArray(propertyTree.value())) {
            ctx.reportIssue(propertyTree, MESSAGE);
          }
        });
      }
    });
  }

  private static boolean isSensitiveString(Tree value) {
    return TextUtils.matchesValue(value, SOURCE_ADDRESS_PREFIX_SENSITIVE::contains).isTrue();
  }

  private static boolean isSensitiveArray(Tree value) {
    if (value instanceof ArrayExpression) {
      ArrayExpression array = (ArrayExpression) value;
      return array.elements().stream()
        .anyMatch(IpRestrictedAdminAccessCheck::isSensitiveString);
    }
    return false;
  }
}
