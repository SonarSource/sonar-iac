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

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checks.utils.CheckUtils;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6382")
public class CertificateBasedAuthenticationCheck extends AbstractArmResourceCheck {

  private static final String MISSING_CERTIFICATE_MESSAGE = "Omitting \"%s\" disables certificate-based authentication. Make sure it is safe here.";
  private static final String DISABLED_CERTIFICATE_MESSAGE = "Make sure that disabling certificate-based authentication is safe here.";
  private static final String ALLOWING_NO_CERTIFICATE_MESSAGE = "Connections without client certificates will be permitted. Make sure it is safe here.";

  private static final List<String> PATH_CONTAINER_APPS = ArmTreeUtils.computePath("configuration/ingress");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.ApiManagement/service/gateways/hostnameConfigurations", CertificateBasedAuthenticationCheck::checkHostnameConfigurations);
    register("Microsoft.App/containerApps", CertificateBasedAuthenticationCheck::checkContainerApps);
  }

  private static void checkHostnameConfigurations(CheckContext ctx, ResourceDeclaration resource) {
    PropertyUtils.get(resource, "negotiateClientCertificate")
      .ifPresentOrElse(
        property -> {
          if (isBooleanFalse(property.value())) {
            ctx.reportIssue(property, DISABLED_CERTIFICATE_MESSAGE);
          }
        },
        () -> ctx.reportIssue(resource.type(), String.format(MISSING_CERTIFICATE_MESSAGE, "negotiateClientCertificate")));
  }

  private static void checkContainerApps(CheckContext ctx, ResourceDeclaration resource) {
    if (isResourceVersionEqualsOrAfter(resource, "2022-10-01")) {
      List<Tree> containers = CheckUtils.resolveProperties(new LinkedList<>(PATH_CONTAINER_APPS), resource);
      for (Tree container : containers) {
        PropertyUtils.get(container, "clientCertificateMode")
          .ifPresentOrElse(
            property -> {
              if (isValue(property.value(), "accept"::equals)) {
                ctx.reportIssue(property, ALLOWING_NO_CERTIFICATE_MESSAGE);
              } else if (isValue(property.value(), "ignore"::equals)) {
                ctx.reportIssue(property, DISABLED_CERTIFICATE_MESSAGE);
              }
            },
            () -> ctx.reportIssue(container, String.format(MISSING_CERTIFICATE_MESSAGE, "clientCertificateMode")));
      }
    }
  }

  private static boolean isBooleanFalse(Tree tree) {
    return ((ArmTree) tree).is(ArmTree.Kind.BOOLEAN_LITERAL) && !((BooleanLiteral) tree).value();
  }

  private static boolean isValue(Tree tree, Predicate<String> predicate) {
    return TextUtils.matchesValue(tree, predicate).isTrue();
  }

  private static boolean isResourceVersionEqualsOrAfter(ResourceDeclaration resource, String version) {
    return resource.version().value().compareTo(version) >= 0;
  }
}
