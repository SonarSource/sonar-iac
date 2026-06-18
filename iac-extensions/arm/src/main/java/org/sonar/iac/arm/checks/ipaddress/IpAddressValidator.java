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
package org.sonar.iac.arm.checks.ipaddress;

import java.util.List;
import java.util.OptionalLong;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;
import org.sonarsource.analyzer.commons.appsec.IpAddressClassifier;

public class IpAddressValidator {

  private static final long DEFAULT_START_IP = 0L;
  private static final long DEFAULT_END_IP = 0xFFFFFFFFL;

  @Nullable
  private final ArmTree startIpAddress;

  @Nullable
  private final ArmTree endIpAddress;

  public IpAddressValidator(@Nullable ArmTree startIpAddress, @Nullable ArmTree endIpAddress) {
    this.startIpAddress = startIpAddress;
    this.endIpAddress = endIpAddress;
  }

  public void reportIssueIfPublicIPAddress(CheckContext ctx, String message, String secondaryLocationMessage) {
    String startLiteral = literalOrNull(startIpAddress);
    String endLiteral = literalOrNull(endIpAddress);

    // Both endpoints are non-extractable (null trees, non-string-literals, or
    // both unparseable strings). Resolving symbolic values is a cross-rule
    // capability handled separately; raising on every unresolved expression
    // would drown genuine findings, so we stay silent here. See SONARIAC-2934.
    OptionalLong start = parseOrEmpty(startLiteral);
    OptionalLong end = parseOrEmpty(endLiteral);
    if (start.isEmpty() && end.isEmpty()) {
      return;
    }

    long startValue = start.orElse(DEFAULT_START_IP);
    long endValue = end.orElse(DEFAULT_END_IP);

    // Inverted ranges (start > end) are caller-side input errors; defaults keep start <= end when one endpoint is unparseable,
    // so an inversion here means the user wrote a reversed pair — flag without consulting the classifier.
    if (startValue > endValue || !IpAddressClassifier.isAddressRangeReserved(startValue, endValue)) {
      reportIssue(ctx, message, secondaryLocationMessage);
    }
  }

  private void reportIssue(CheckContext ctx, String message, String secondaryLocationMessage) {
    Tree tree = (startIpAddress != null) ? startIpAddress : endIpAddress;
    Tree secondary = (startIpAddress != null && endIpAddress != null) ? endIpAddress : null;

    if (secondary != null) {
      SecondaryLocation secondaryLocation = new SecondaryLocation(secondary, secondaryLocationMessage);
      ctx.reportIssue(tree, message, List.of(secondaryLocation));
    } else {
      ctx.reportIssue(tree, message);
    }
  }

  private static OptionalLong parseOrEmpty(@Nullable String literal) {
    return literal == null ? OptionalLong.empty() : IpAddressClassifier.parseIpv4SingleAddress(literal);
  }

  @CheckForNull
  private static String literalOrNull(@Nullable ArmTree tree) {
    if (tree != null && tree.is(ArmTree.Kind.STRING_LITERAL)) {
      return TextUtils.getValue(tree).orElse(null);
    }
    return null;
  }
}
