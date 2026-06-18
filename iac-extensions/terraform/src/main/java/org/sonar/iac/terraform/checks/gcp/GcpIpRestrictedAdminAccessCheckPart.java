/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks.gcp;

import java.util.List;
import java.util.function.Predicate;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.ResourceSymbol;
import org.sonarsource.analyzer.commons.appsec.IpAddressClassifier;

import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils.MESSAGE;
import static org.sonar.iac.terraform.checks.IpRestrictedAdminAccessCheck.SECONDARY_MSG;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isTrue;

public class GcpIpRestrictedAdminAccessCheckPart extends AbstractNewResourceCheck {

  private static final Predicate<ExpressionTree> RANGE_CONTAINS_SENSITIVE_PORTS = range -> TextUtils
    .matchesValue(range, IpRestrictedAdminAccessCheckUtils::rangeContainsSshOrRdpPort)
    .isTrue();

  private static final Predicate<ExpressionTree> SENSITIVE_IP_RANGE = range -> TextUtils.matchesValue(range, IpAddressClassifier::isUnrestrictedCidr).isTrue();

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
