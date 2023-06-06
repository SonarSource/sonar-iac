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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

@Rule(key = "S6321")
public class IpRestrictedAdminAccessCheck implements IacCheck {

  private static final String MESSAGE = "Restrict IP addresses authorized to access administration services.";
  private static final String RESOURCE_TYPE = "Microsoft.Network/networkSecurityGroups/securityRules";
  private static final Set<String> SOURCE_ADDRESS_PREFIX_SENSITIVE = Set.of("*", "0.0.0.0/0", "::/0", "Internet");
  private static final Set<String> SENSITIVE_PROTOCOL = Set.of("*", "TCP");
  private static final Set<String> SENSITIVE_PORT = Set.of("*", "22", "3389");
  private static final Pattern patternPortRange = Pattern.compile("^(?<min>\\d+)-(?<max>\\d+)$");

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, (ctx, resource) -> {
      if (RESOURCE_TYPE.equals(resource.type().value())) {
        ResourcePropertiesChecker checker = new ResourcePropertiesChecker(resource);
        if (checker.isSensitive()) {
          checker.reportIssue(ctx);
        }
      }
    });
  }

  static class ResourcePropertiesChecker {
    StringLiteral name;
    Optional<Tree> direction;
    Optional<Tree> access;
    Optional<Tree> protocol;
    Optional<Tree> destinationPortRange;
    Optional<Tree> destinationPortRanges;
    Optional<Tree> sourceAddressPrefix;
    Optional<Tree> sourceAddressPrefixes;

    ResourcePropertiesChecker(ResourceDeclaration resource) {
      name = resource.name();
      direction = PropertyUtils.value(resource, "direction");
      access = PropertyUtils.value(resource, "access");
      protocol = PropertyUtils.value(resource, "protocol");
      destinationPortRange = PropertyUtils.value(resource, "destinationPortRange");
      destinationPortRanges = PropertyUtils.value(resource, "destinationPortRanges");
      sourceAddressPrefix = PropertyUtils.value(resource, "sourceAddressPrefix");
      sourceAddressPrefixes = PropertyUtils.value(resource, "sourceAddressPrefixes");
    }

    boolean isSensitive() {
      return direction.filter(tree -> TextUtils.matchesValue(tree, "Inbound"::equalsIgnoreCase).isTrue()).isPresent()
        && access.filter(tree -> TextUtils.matchesValue(tree, "Allow"::equalsIgnoreCase).isTrue()).isPresent()
        && protocol.filter(tree -> TextUtils.matchesValue(tree, str -> SENSITIVE_PROTOCOL.contains(str.toUpperCase())).isTrue()).isPresent()
        && (destinationPortRange.filter(ResourcePropertiesChecker::isSensitivePort).isPresent()
          || destinationPortRanges.filter(tree -> isArrayWith(tree, ResourcePropertiesChecker::isSensitivePort)).isPresent())
        && (sourceAddressPrefix.filter(ResourcePropertiesChecker::isSensitiveSourceAddressString).isPresent()
          || sourceAddressPrefixes.filter(tree -> isArrayWith(tree, ResourcePropertiesChecker::isSensitiveSourceAddressString)).isPresent());
    }

    void reportIssue(CheckContext ctx) {
      List<SecondaryLocation> secondaryLocations = new ArrayList<>();

      sourceAddressPrefix.ifPresent(tree -> secondaryLocations.add(new SecondaryLocation(tree, "Sensitive source address prefix")));
      sourceAddressPrefixes.ifPresent(tree -> secondaryLocations.add(new SecondaryLocation(tree, "Sensitive source(s) address prefix(es)")));
      secondaryLocations.add(new SecondaryLocation(direction.get(), "Sensitive direction"));
      secondaryLocations.add(new SecondaryLocation(direction.get(), "Sensitive direction"));
      secondaryLocations.add(new SecondaryLocation(access.get(), "Sensitive access"));
      secondaryLocations.add(new SecondaryLocation(protocol.get(), "Sensitive protocol"));
      destinationPortRange.ifPresent(tree -> secondaryLocations.add(new SecondaryLocation(tree, "Sensitive destination port range")));
      destinationPortRanges.ifPresent(tree -> secondaryLocations.add(new SecondaryLocation(tree, "Sensitive destination(s) port range(s)")));

      ctx.reportIssue(name, MESSAGE, secondaryLocations);
    }

    private static boolean isArrayWith(Tree tree, Predicate<Tree> predicate) {
      if (tree instanceof ArrayExpression) {
        ArrayExpression array = (ArrayExpression) tree;
        return array.elements().stream()
          .anyMatch(predicate);
      }
      return false;
    }

    private static boolean isSensitiveSourceAddressString(Tree value) {
      return TextUtils.matchesValue(value, SOURCE_ADDRESS_PREFIX_SENSITIVE::contains).isTrue();
    }

    private static boolean isSensitivePort(Tree tree) {
      return TextUtils.getValue(tree)
        .filter(value -> SENSITIVE_PORT.contains(value) || isSensitivePortRange(value))
        .isPresent();
    }

    private static boolean isSensitivePortRange(String portRange) {
      Matcher matcher = patternPortRange.matcher(portRange);
      if (matcher.find()) {
        int min = Integer.parseInt(matcher.group("min"));
        int max = Integer.parseInt(matcher.group("max"));
        return inRange(22, min, max) || inRange(3389, min, max);
      }
      return false;
    }

    private static boolean inRange(int number, int min, int max) {
      return number >= min && number <= max;
    }
  }
}
