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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckBase;

@Rule(key = "S6321")
public class IpRestrictedAdminAccessCheck extends IpRestrictedAdminAccessCheckBase implements IacCheck {

  private static final String SECURITY_RULES = "securityRules";
  private static final String PROPERTIES = "properties";
  private static final String NETWORK_SECURITY_GROUP = "networkSecurityGroup";
  private static final String SUBNETS = "subnets";
  private static final String SUBNET = "subnet";
  private static final String ARRAY_TOKEN = "*";
  private static final Map<String, List<String>> PATH_PER_RESOURCE_TYPE = Map.of(
    "Microsoft.Network/networkSecurityGroups/securityRules", List.of(),
    "Microsoft.Network/networkSecurityGroup", List.of(SECURITY_RULES, ARRAY_TOKEN, PROPERTIES),
    "Microsoft.Network/virtualNetworks/subnets", List.of(NETWORK_SECURITY_GROUP, PROPERTIES, SECURITY_RULES, ARRAY_TOKEN, PROPERTIES),
    "Microsoft.Network/virtualNetworks", List.of(SUBNETS, ARRAY_TOKEN, PROPERTIES, NETWORK_SECURITY_GROUP, PROPERTIES, SECURITY_RULES, ARRAY_TOKEN, PROPERTIES),
    "Microsoft.Network/networkInterfaces",
    List.of("ipConfigurations", ARRAY_TOKEN, PROPERTIES, SUBNET, PROPERTIES, NETWORK_SECURITY_GROUP, PROPERTIES, SECURITY_RULES, ARRAY_TOKEN, PROPERTIES));
  private static final Set<String> SOURCE_ADDRESS_PREFIX_SENSITIVE = Set.of("*", ALL_IPV4, ALL_IPV6, "Internet");
  private static final Set<String> SENSITIVE_PROTOCOL = Set.of("*", "TCP");

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, (ctx, resource) -> {
      List<String> path = PATH_PER_RESOURCE_TYPE.get(resource.type().value());
      if (path != null) {
        List<Tree> listProperties = resolveProperties(new LinkedList<>(path), resource);
        for (Tree properties : listProperties) {
          ResourceWithIpRestrictedAdminAccessChecker checker = new ResourceWithIpRestrictedAdminAccessChecker(resource, properties);
          if (checker.isSensitive()) {
            checker.reportIssue(ctx);
          }
        }
      }
    });
  }

  /**
   * This method is used to retrieve the list of tree elements which can be resolved using a provided queue of path.
   * The result is a list of Tree because it can resolve to multiple element since it is supporting the token '*' to means that an array is expected.
   * Example:
   *   Provided list of path: "connections", "*", "entry"
   *   Provided Tree representation:
   *   {
   *     "connections": [
   *       { "entry":"val1" }, --> will be in the result list
   *       { "entry":"val2" }  --> will also be in the result list
   *     ]
   *   }
   */
  private static List<Tree> resolveProperties(Queue<String> path, Tree tree) {
    while (!path.isEmpty() && tree != null) {
      String nextPath = path.poll();
      if (nextPath.equals(ARRAY_TOKEN)) {
        if (tree instanceof ArrayExpression) {
          ArrayExpression array = (ArrayExpression) tree;
          List<Tree> trees = new ArrayList<>();
          array.elements().forEach(element -> trees.addAll(resolveProperties(new LinkedList<>(path), element)));
          return trees;
        } else {
          return Collections.emptyList();
        }
      } else {
        tree = PropertyUtils.value(tree, nextPath).orElse(null);
      }
    }
    return tree != null ? List.of(tree) : Collections.emptyList();
  }

  static class ResourceWithIpRestrictedAdminAccessChecker {
    StringLiteral name;
    @Nullable
    Tree direction;
    @Nullable
    Tree access;
    @Nullable
    Tree protocol;
    @Nullable
    Tree destinationPortRange;
    @Nullable
    Tree destinationPortRanges;
    @Nullable
    Tree sourceAddressPrefix;
    @Nullable
    Tree sourceAddressPrefixes;

    ResourceWithIpRestrictedAdminAccessChecker(ResourceDeclaration resource, Tree properties) {
      name = resource.name();
      direction = PropertyUtils.value(properties, "direction").orElse(null);
      access = PropertyUtils.value(properties, "access").orElse(null);
      protocol = PropertyUtils.value(properties, "protocol").orElse(null);
      destinationPortRange = PropertyUtils.value(properties, "destinationPortRange").orElse(null);
      destinationPortRanges = PropertyUtils.value(properties, "destinationPortRanges").orElse(null);
      sourceAddressPrefix = PropertyUtils.value(properties, "sourceAddressPrefix").orElse(null);
      sourceAddressPrefixes = PropertyUtils.value(properties, "sourceAddressPrefixes").orElse(null);
    }

    boolean isSensitive() {
      return TextUtils.isValue(direction, "Inbound").isTrue()
        && TextUtils.isValue(access, "Allow").isTrue()
        && TextUtils.matchesValue(protocol, str -> SENSITIVE_PROTOCOL.contains(str.toUpperCase(Locale.ROOT))).isTrue()
        && (isSensitivePort(destinationPortRange) || isArrayWith(destinationPortRanges, this::isSensitivePort))
        && (isSensitiveSourceAddressString(sourceAddressPrefix) || isArrayWith(sourceAddressPrefixes, this::isSensitiveSourceAddressString));
    }

    void reportIssue(CheckContext ctx) {
      List<SecondaryLocation> secondaryLocations = new ArrayList<>();
      if (sourceAddressPrefix != null) {
        secondaryLocations.add(new SecondaryLocation(sourceAddressPrefix, "Sensitive source address prefix"));
      } else {
        secondaryLocations.add(new SecondaryLocation(sourceAddressPrefixes, "Sensitive source(s) address prefix(es)"));
      }
      secondaryLocations.add(new SecondaryLocation(direction, "Sensitive direction"));
      secondaryLocations.add(new SecondaryLocation(access, "Sensitive access"));
      secondaryLocations.add(new SecondaryLocation(protocol, "Sensitive protocol"));
      if (destinationPortRange != null) {
        secondaryLocations.add(new SecondaryLocation(destinationPortRange, "Sensitive destination port range"));
      } else {
        secondaryLocations.add(new SecondaryLocation(destinationPortRanges, "Sensitive destination(s) port range(s)"));
      }

      ctx.reportIssue(name, MESSAGE, secondaryLocations);
    }

    private static boolean isArrayWith(@Nullable Tree tree, Predicate<Tree> predicate) {
      if (tree instanceof ArrayExpression) {
        ArrayExpression array = (ArrayExpression) tree;
        return array.elements().stream()
          .anyMatch(predicate);
      }
      return false;
    }

    private boolean isSensitiveSourceAddressString(Tree value) {
      return TextUtils.matchesValue(value, SOURCE_ADDRESS_PREFIX_SENSITIVE::contains).isTrue();
    }

    private boolean isSensitivePort(Tree tree) {
      return TextUtils.getValue(tree)
        .filter(IpRestrictedAdminAccessCheckBase::rangeContainsSshOrRdpPort)
        .isPresent();
    }
  }
}
