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

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.common.checks.PropertyUtils.has;
import static org.sonar.iac.common.checks.PropertyUtils.value;
import static org.sonar.iac.common.checks.PropertyUtils.valueIs;

@Rule(key = "S6321")
public class IpRestrictedAdminAccessCheck implements IacCheck {

  private static final List<String> TYPES = List.of(
    "Microsoft.Network/networkSecurityGroups/securityRules");
  private static final List<String> SOURCE_ADDRESS_PREFIX_SENSITIVE = List.of("*", "0.0.0.0/0", "::/0", "Internet");

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, (ctx, resource) -> {
      if (TYPES.contains(resource.type()) && valueIs(resource, "sourceAddressPrefix", this::isDestinationSensitive)) {
        ctx.reportIssue(value(resource, "sourceAddressPrefix").get(), "Restrict IP addresses authorized to access administration services");
      }
    });
  }

  private boolean isDestinationSensitive(Tree tree) {
    Property property = (Property) tree;
    return isStringValue(property.value(), SOURCE_ADDRESS_PREFIX_SENSITIVE::contains);
  }

  private boolean isStringValue(Expression expression, Predicate<String> predicate) {
    return expression.is(ArmTree.Kind.STRING_LITERAL) && predicate.test(((StringLiteral) expression).value());
  }
}
