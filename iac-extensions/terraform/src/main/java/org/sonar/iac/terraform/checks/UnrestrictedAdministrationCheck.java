/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.PrefixExpressionTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

@Rule(key = "S6321")
public class UnrestrictedAdministrationCheck extends AbstractResourceCheck {

  public static final String MESSAGE = "Restrict IP addresses authorized to access administration services";
  public static final int SSH_PORT = 22;
  public static final int RDP_PORT = 3389;

  @Override
  protected void registerResourceChecks() {
    register(UnrestrictedAdministrationCheck::checkSecurityGroup, "aws_security_group");
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
      ctx.reportIssue(defaultRouteCidrTree.get(), MESSAGE, new SecondaryLocation(ipProtocol.get(), "Related protocol setting"));
    } else if (ipProtocol.isPresent() && TextUtils.isValue(ipProtocol.get(), "tcp").isTrue()) {
      checkTcpPorts(ctx, ingress, defaultRouteCidrTree.get(), ipProtocol.get());
    }
  }

  private static void checkTcpPorts(CheckContext ctx, Tree rule, Tree defaultRouteCidrTree, Tree ipProtocol) {
    Optional<Tree> fromPort = PropertyUtils.value(rule, "from_port");
    Optional<Tree> toPort = PropertyUtils.value(rule, "to_port");
    if (fromPort.isPresent() && toPort.isPresent() && rangeContainsSshOrRdpPorts(fromPort.get(), toPort.get())) {
      List<SecondaryLocation> secondaryLocations = new ArrayList<>();
      secondaryLocations.add(new SecondaryLocation(ipProtocol, "Related protocol setting"));
      secondaryLocations.add(new SecondaryLocation(fromPort.get(), "Port range start"));
      secondaryLocations.add(new SecondaryLocation(toPort.get(), "Port range end"));
      ctx.reportIssue(defaultRouteCidrTree, MESSAGE, secondaryLocations);
    }
  }

  private static boolean isAllProtocols(Tree tree) {
    return tree instanceof PrefixExpressionTree && "-".equals(((PrefixExpressionTree) tree).prefix().value()) &&
      TextUtils.isValue(((PrefixExpressionTree) tree).expression(), "1").isTrue();
  }

  private static boolean rangeContainsSshOrRdpPorts(Tree from, Tree to) {
    Optional<Integer> fromIntValue = TextUtils.getIntValue(from);
    Optional<Integer> toIntValue = TextUtils.getIntValue(to);
    return (fromIntValue.isPresent() && toIntValue.isPresent()) &&
      ((fromIntValue.get() == 0 && toIntValue.get() == 0) ||
        ((SSH_PORT >= fromIntValue.get() && SSH_PORT <= toIntValue.get()) || (RDP_PORT >= fromIntValue.get() && RDP_PORT <= toIntValue.get())));
  }

  private static Optional<TupleTree> getDefaultRouteCidr(BlockTree ingress) {
    Optional<TupleTree> optCidrIp = PropertyUtils.value(ingress, "cidr_blocks", TupleTree.class)
      .filter(c -> containsValue(c, "0.0.0.0/0"));
    Optional<TupleTree> optCidrIpv6 = PropertyUtils.value(ingress, "ipv6_cidr_blocks", TupleTree.class)
      .filter(c -> containsValue(c, "::/0"));

    return optCidrIp.isPresent() ? optCidrIp : optCidrIpv6;
  }

  private static boolean containsValue(TupleTree tupleTree, String value) {
    return tupleTree.elements().trees().stream().anyMatch(t -> TextUtils.isValue(t, value).isTrue());
  }
}
