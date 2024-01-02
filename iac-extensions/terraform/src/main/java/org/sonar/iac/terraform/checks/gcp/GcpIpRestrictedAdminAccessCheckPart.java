/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils.ALL_IPV4;
import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils.ALL_IPV6;
import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils.MESSAGE;
import static org.sonar.iac.terraform.checks.IpRestrictedAdminAccessCheck.SECONDARY_MSG;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isTrue;

public class GcpIpRestrictedAdminAccessCheckPart extends AbstractNewResourceCheck {

  private static final Set<String> SENSITIVE_PREFIXES = Set.of(ALL_IPV4, ALL_IPV6, "0::0/0", "::0/0");

  private static final Predicate<ExpressionTree> RANGE_CONTAINS_SENSITIVE_PORTS = range -> TextUtils
    .matchesValue(range, IpRestrictedAdminAccessCheckUtils::rangeContainsSshOrRdpPort)
    .isTrue();

  private static final Predicate<ExpressionTree> SENSITIVE_IP_RANGE = range -> TextUtils.matchesValue(range, SENSITIVE_PREFIXES::contains).isTrue();

  @Override
  protected void registerResourceConsumer() {
    register(List.of("google_compute_firewall"), this::checkFirewall);
  }

  private void checkFirewall(ResourceSymbol firewall) {
    // Check preconditions for a sensitive firewall
    if (firewall.attribute("direction").is(equalTo("EGRESS"))
      || firewall.attribute("source_tags").isPresent()
      || firewall.attribute("disabled").is(isTrue())) {
      return;
    }

    SecondaryLocation[] sensitivePortLocations = firewall.blocks("allow")
      .filter(allow -> allow.attribute("protocol").is(equalTo("tcp")))
      .flatMap(allow -> allow.list("ports").getItemIf(RANGE_CONTAINS_SENSITIVE_PORTS))
      .map(sensitivePorts -> new SecondaryLocation(sensitivePorts, SECONDARY_MSG))
      .toArray(SecondaryLocation[]::new);

    if (sensitivePortLocations.length > 0) {
      firewall.list("source_ranges").reportItemIf(SENSITIVE_IP_RANGE, MESSAGE, sensitivePortLocations);
    }
  }
}
