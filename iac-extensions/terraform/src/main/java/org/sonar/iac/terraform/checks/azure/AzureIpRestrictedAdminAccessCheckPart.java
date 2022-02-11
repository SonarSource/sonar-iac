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
package org.sonar.iac.terraform.checks.azure;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;
import org.sonar.iac.terraform.checks.IpRestrictedAdminAccessCheck;

import static org.sonar.iac.terraform.checks.IpRestrictedAdminAccessCheck.ALL_IPV4;
import static org.sonar.iac.terraform.checks.IpRestrictedAdminAccessCheck.ALL_IPV6;
import static org.sonar.iac.terraform.checks.IpRestrictedAdminAccessCheck.MESSAGE;
import static org.sonar.iac.terraform.checks.IpRestrictedAdminAccessCheck.SECONDARY_MSG;

public class AzureIpRestrictedAdminAccessCheckPart extends AbstractResourceCheck {

  private static final Set<String> SENSITIVE_PREFIXES = Set.of("*", ALL_IPV4, ALL_IPV6);

  @Override
  protected void registerResourceChecks() {
    register(AzureIpRestrictedAdminAccessCheckPart::checkNetworkSecurityGroup, "azurerm_network_security_group");
    register(AzureIpRestrictedAdminAccessCheckPart::checkNetworkSecurityRule, "azurerm_network_security_rule");
  }

  public static void checkNetworkSecurityGroup(CheckContext ctx, BlockTree resource) {
    PropertyUtils.getAll(resource, "security_rule", BlockTree.class).forEach(rule -> checkNetworkSecurityRule(ctx, rule));
  }

  private static void checkNetworkSecurityRule(CheckContext ctx, BlockTree rule) {
    if (hasAttributeWithMatchingValue(rule,"direction", "Inbound"::equals)
      && hasAttributeWithMatchingValue(rule, "access", "Allow"::equals)
      && hasAttributeWithMatchingValue(rule, "protocol", p -> "Tcp".equals(p) || "*".equals(p))) {
      checkSecurityRule(ctx, rule);
    }
  }

  private static void checkSecurityRule(CheckContext ctx, BlockTree rule) {
    sensitiveDestinationPortRange(rule).ifPresent(sensitivePort ->
      sensitiveSourcePrefix(rule).ifPresent(sensitivePrefix ->
        ctx.reportIssue(sensitivePrefix, MESSAGE, new SecondaryLocation(sensitivePort, SECONDARY_MSG))));
  }

  private static Optional<ExpressionTree> sensitiveDestinationPortRange(BlockTree rule) {
    Predicate<ExpressionTree> rangeContainsSensitivePort = range -> TextUtils.getValue(range)
      .filter(IpRestrictedAdminAccessCheck::rangeContainsSshOrRdpPort).isPresent();

    return PropertyUtils.get(rule, "destination_port_range", AttributeTree.class)
      .map(AttributeTree::value)
      .filter(rangeContainsSensitivePort)
      .or(() -> expressionInAttributeTuple(rule, "destination_port_ranges", rangeContainsSensitivePort));
  }

  private static Optional<ExpressionTree> sensitiveSourcePrefix(BlockTree rule) {
    Predicate<ExpressionTree> isSensitivePrefix = prefixExpression -> TextUtils.matchesValue(prefixExpression, SENSITIVE_PREFIXES::contains).isTrue();

    return PropertyUtils.get(rule, "source_address_prefix", AttributeTree.class)
      .map(AttributeTree::value)
      .filter(isSensitivePrefix)
      .or(() -> expressionInAttributeTuple(rule, "source_address_prefixes", isSensitivePrefix));
  }

  /**
   * If the attribute in the block exists and the attribute's value is a tuple,
   * return the first expression in the tuple elements which matches the condition
   */
  private static Optional<ExpressionTree> expressionInAttributeTuple(BlockTree block, String attribute, Predicate<ExpressionTree> predicate) {
    return PropertyUtils.get(block, attribute, AttributeTree.class)
      .map(AttributeTree::value)
      .filter(TupleTree.class::isInstance)
      .map(TupleTree.class::cast)
      .flatMap(tupleTree -> tupleTree.elements().trees().stream().filter(predicate).findFirst());
  }

  /**
   * Check if rule block contains an attribute and the attribute's value matches the given condition
   */
  private static boolean hasAttributeWithMatchingValue(BlockTree rule, String attribute, Predicate<String> stringPredicate) {
    return PropertyUtils.get(rule, attribute, AttributeTree.class)
      .filter(attr -> TextUtils.matchesValue(attr.value(), stringPredicate).isTrue())
      .isPresent();
  }
}
