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
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        ResourceWithIpRestrictedAdminAccessChecker checker = new ResourceWithIpRestrictedAdminAccessChecker(resource);
        if (checker.isSensitive()) {
          checker.reportIssue(ctx);
        }
      }
    });
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

    ResourceWithIpRestrictedAdminAccessChecker(ResourceDeclaration resource) {
      name = resource.name();
      direction = PropertyUtils.value(resource, "direction").orElse(null);
      access = PropertyUtils.value(resource, "access").orElse(null);
      protocol = PropertyUtils.value(resource, "protocol").orElse(null);
      destinationPortRange = PropertyUtils.value(resource, "destinationPortRange").orElse(null);
      destinationPortRanges = PropertyUtils.value(resource, "destinationPortRanges").orElse(null);
      sourceAddressPrefix = PropertyUtils.value(resource, "sourceAddressPrefix").orElse(null);
      sourceAddressPrefixes = PropertyUtils.value(resource, "sourceAddressPrefixes").orElse(null);
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
