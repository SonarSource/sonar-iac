/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.checks;

import java.util.List;
import java.util.function.Consumer;
import org.sonar.iac.arm.checkdsl.ContextualArray;
import org.sonar.iac.arm.checkdsl.ContextualMap;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.checks.ipaddress.IpAddressValidator;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.Tree;

class PublicNetworkAccessIpRangeCheckPart extends AbstractArmResourceCheck {

  private static final String PUBLIC_IP_ADDRESS_MESSAGE = "Make sure that allowing public IP addresses is safe here.";
  private static final String PUBLIC_IP_ADDRESS_MESSAGE_SECONDARY_LOCATION = "and here";

  private static final List<String> PUBLIC_IP_ADDRESS_RANGE_TYPES = List.of(
    "Microsoft.DBForPostgreSql/flexibleServers/firewallRules",
    "Microsoft.DBforMariaDB/servers/firewallRules",
    "Microsoft.DBforMySQL/flexibleServers/firewallRules",
    "Microsoft.DBforMySQL/servers/firewallRules",
    "Microsoft.DBforPostgreSQL/flexibleServers/firewallRules",
    "Microsoft.DBforPostgreSQL/serverGroupsv2/firewallRules",
    "Microsoft.DBforPostgreSQL/servers/firewallRules",
    "Microsoft.DataLakeAnalytics/accounts/firewallRules",
    "Microsoft.DataLakeStore/accounts/firewallRules",
    "Microsoft.DocumentDB/mongoClusters/firewallRules",
    "Microsoft.Sql/servers/firewallRules",
    "Microsoft.Synapse/workspaces/firewallRules");

  private static final List<String> PUBLIC_IP_ADDRESS_RANGE_IN_FIREWALL_RULES_TYPES = List.of(
    "Microsoft.Blockchain/blockchainMembers",
    "Microsoft.Blockchain/blockchainMembers/transactionNodes");

  private static final List<String> PUBLIC_IP_ADDRESS_RANGE_IN_PROPERTIES_FIREWALL_RULES_TYPES = List.of(
    "Microsoft.DataLakeAnalytics/accounts",
    "Microsoft.DataLakeStore/accounts");

  @Override
  protected void registerResourceConsumer() {
    register(PUBLIC_IP_ADDRESS_RANGE_TYPES, PublicNetworkAccessIpRangeCheckPart::checkIpRange);
    register(PUBLIC_IP_ADDRESS_RANGE_IN_FIREWALL_RULES_TYPES, checkIpRangeInFirewallRules());
    register(PUBLIC_IP_ADDRESS_RANGE_IN_PROPERTIES_FIREWALL_RULES_TYPES, checkIpRangeInFirewallRulesProperties());
  }

  private static <S extends ContextualMap<S, T>, T extends HasProperties & Tree> void checkIpRange(ContextualMap<S, T> resource) {
    ContextualProperty startIpAddress = resource.property("startIpAddress");
    ContextualProperty endIpAddress = resource.property("endIpAddress");
    Expression startValue = startIpAddress.valueOrNull();
    Expression endValue = endIpAddress.valueOrNull();
    IpAddressValidator validator = new IpAddressValidator(startValue, endValue);
    validator.reportIssueIfPublicIPAddress(resource.ctx, PUBLIC_IP_ADDRESS_MESSAGE, PUBLIC_IP_ADDRESS_MESSAGE_SECONDARY_LOCATION);
  }

  private static Consumer<ContextualResource> checkIpRangeInFirewallRules() {
    return resource -> {
      ContextualArray list = resource.list("firewallRules");
      list.objects().forEach(PublicNetworkAccessIpRangeCheckPart::checkIpRange);
    };
  }

  private static Consumer<ContextualResource> checkIpRangeInFirewallRulesProperties() {
    return resource -> {
      ContextualArray list = resource.list("firewallRules");
      list.objects().forEach(rule -> {
        ContextualObject properties = rule.object("properties");
        checkIpRange(properties);
      });
    };
  }
}
