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
package org.sonar.iac.terraform.checks.aws;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.PrefixExpressionTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils.ALL_IPV4;
import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils.ALL_IPV6;
import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils.MESSAGE;
import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckUtils.rangeContainsSshOrRdpPort;
import static org.sonar.iac.terraform.checks.IpRestrictedAdminAccessCheck.SECONDARY_MSG;

public class AwsIpRestrictedAdminAccessCheckPart extends AbstractResourceCheck {

  @Override
  protected void registerResourceChecks() {
    register(AwsIpRestrictedAdminAccessCheckPart::checkSecurityGroup, "aws_security_group");
  }

  private static void checkSecurityGroup(CheckContext ctx, BlockTree resource) {
    PropertyUtils.getAll(resource, "ingress", BlockTree.class).forEach(i -> checkIngress(ctx, i));
  }

  private static void checkIngress(CheckContext ctx, BlockTree ingress) {
    Optional<TupleTree> defaultRouteCidrTree = getDefaultRouteCidr(ingress);
    if (defaultRouteCidrTree.isEmpty()) {
      return;
    }

    Optional<Tree> ipProtocol = PropertyUtils.value(ingress, "protocol");
    if (ipProtocol.isPresent() && isAllProtocols(ipProtocol.get())) {
      ctx.reportIssue(defaultRouteCidrTree.get(), MESSAGE, new SecondaryLocation(ipProtocol.get(), SECONDARY_MSG));
    } else if (ipProtocol.isPresent() && TextUtils.isValue(ipProtocol.get(), "tcp").isTrue()) {
      checkTcpPorts(ctx, ingress, defaultRouteCidrTree.get(), ipProtocol.get());
    }
  }

  private static void checkTcpPorts(CheckContext ctx, Tree rule, Tree defaultRouteCidrTree, Tree ipProtocol) {
    Optional<Tree> fromPort = PropertyUtils.value(rule, "from_port");
    Optional<Tree> toPort = PropertyUtils.value(rule, "to_port");
    if (fromPort.isPresent() && toPort.isPresent() && rangeContainsSensitivePort(fromPort.get(), toPort.get())) {
      List<SecondaryLocation> secondaryLocations = new ArrayList<>();
      secondaryLocations.add(new SecondaryLocation(ipProtocol, SECONDARY_MSG));
      secondaryLocations.add(new SecondaryLocation(fromPort.get(), "Port range start."));
      secondaryLocations.add(new SecondaryLocation(toPort.get(), "Port range end."));
      ctx.reportIssue(defaultRouteCidrTree, MESSAGE, secondaryLocations);
    }
  }

  private static boolean isAllProtocols(Tree tree) {
    return tree instanceof PrefixExpressionTree && "-".equals(((PrefixExpressionTree) tree).prefix().value()) &&
      TextUtils.isValue(((PrefixExpressionTree) tree).expression(), "1").isTrue();
  }

  private static boolean rangeContainsSensitivePort(Tree from, Tree to) {
    Optional<Integer> fromIntValue = TextUtils.getIntValue(from);
    Optional<Integer> toIntValue = TextUtils.getIntValue(to);
    return (fromIntValue.isPresent() && toIntValue.isPresent()) &&
      ((fromIntValue.get() == 0 && toIntValue.get() == 0) || rangeContainsSshOrRdpPort(fromIntValue.get(), toIntValue.get()));
  }

  private static Optional<TupleTree> getDefaultRouteCidr(BlockTree ingress) {
    Optional<TupleTree> optCidrIp = PropertyUtils.value(ingress, "cidr_blocks", TupleTree.class)
      .filter(c -> containsValue(c, ALL_IPV4));
    Optional<TupleTree> optCidrIpv6 = PropertyUtils.value(ingress, "ipv6_cidr_blocks", TupleTree.class)
      .filter(c -> containsValue(c, ALL_IPV6));

    return optCidrIp.isPresent() ? optCidrIp : optCidrIpv6;
  }

  private static boolean containsValue(TupleTree tupleTree, String value) {
    return tupleTree.elements().trees().stream().anyMatch(t -> TextUtils.isValue(t, value).isTrue());
  }
}
