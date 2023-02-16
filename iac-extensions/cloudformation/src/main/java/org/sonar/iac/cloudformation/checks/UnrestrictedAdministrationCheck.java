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
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6321")
public class UnrestrictedAdministrationCheck extends AbstractResourceCheck {

  public static final String MESSAGE = "Restrict IP addresses authorized to access administration services";
  public static final int SSH_PORT = 22;
  public static final int RDP_PORT = 3389;

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::EC2::SecurityGroup")) {
      checkSecurityGroup(ctx, resource);
    }
  }

  private static void checkSecurityGroup(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), "SecurityGroupIngress", SequenceTree.class)
      .ifPresent(sequenceTree -> sequenceTree.elements().forEach(r -> checkIngressRule(ctx, r)));
  }

  private static void checkIngressRule(CheckContext ctx, Tree rule) {
    Optional<Tree> defaultRouteCidrTree = getDefaultRouteCidr(rule);
    if (!defaultRouteCidrTree.isPresent()) {
      return;
    }

    Optional<Tree> ipProtocol = PropertyUtils.value(rule, "IpProtocol");
    if (ipProtocol.isPresent() && TextUtils.isValue(ipProtocol.get(), "-1").isTrue()) {
      ctx.reportIssue(defaultRouteCidrTree.get(), MESSAGE, new SecondaryLocation(ipProtocol.get(), "Related protocol setting"));
    } else if (ipProtocol.isPresent() && TextUtils.isValue(ipProtocol.get(), "tcp").isTrue()) {
      checkTcpPorts(ctx, rule, defaultRouteCidrTree.get(), ipProtocol.get());
    }
  }

  private static Optional<Tree> getDefaultRouteCidr(Tree rule) {
    Optional<Tree> optCidrIp = PropertyUtils.value(rule, "CidrIp")
      .filter(c -> TextUtils.isValue(c, "0.0.0.0/0").isTrue());
    Optional<Tree> optCidrIpv6 = PropertyUtils.value(rule, "CidrIpv6")
      .filter(c -> TextUtils.isValue(c, "::/0").isTrue());
    return optCidrIp.isPresent() ? optCidrIp : optCidrIpv6;
  }

  private static void checkTcpPorts(CheckContext ctx, Tree rule, Tree defaultRouteCidrTree, Tree ipProtocol) {
    Optional<Tree> fromPort = PropertyUtils.value(rule, "FromPort");
    Optional<Tree> toPort = PropertyUtils.value(rule, "ToPort");
    if (fromPort.isPresent() && toPort.isPresent() && rangeContainsSshOrRdpPorts(fromPort.get(), toPort.get())) {
      List<SecondaryLocation> secondaryLocations = new ArrayList<>();
      secondaryLocations.add(new SecondaryLocation(ipProtocol, "Related protocol setting"));
      secondaryLocations.add(new SecondaryLocation(fromPort.get(), "Port range start"));
      secondaryLocations.add(new SecondaryLocation(toPort.get(), "Port range end"));
      ctx.reportIssue(defaultRouteCidrTree, MESSAGE, secondaryLocations);
    }
  }

  private static boolean rangeContainsSshOrRdpPorts(Tree from, Tree to) {
    Optional<Integer> fromIntValue = TextUtils.getIntValue(from);
    Optional<Integer> toIntValue = TextUtils.getIntValue(to);
    return (fromIntValue.isPresent() && toIntValue.isPresent()) &&
        ((SSH_PORT >= fromIntValue.get() && SSH_PORT <= toIntValue.get()) || (RDP_PORT >= fromIntValue.get() && RDP_PORT <= toIntValue.get()));
  }
}
