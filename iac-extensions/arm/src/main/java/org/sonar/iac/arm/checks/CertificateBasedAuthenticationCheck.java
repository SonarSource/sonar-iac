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

import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;

@Rule(key = "S6382")
public class CertificateBasedAuthenticationCheck extends AbstractArmResourceCheck {

  private static final String MISSING_CERTIFICATE = "Omitting \"%s\" disables certificate-based authentication. Make sure it is safe here.";
  private static final String DISABLED_CERTIFICATE = "Make sure that disabling certificate-based authentication is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.ApiManagement/service/gateways/hostnameConfigurations", CertificateBasedAuthenticationCheck::checkNegotiateClientCertificate);
  }

  private static void checkNegotiateClientCertificate(CheckContext ctx, ResourceDeclaration resource) {
    PropertyUtils.value(resource, "negotiateClientCertificate")
      .ifPresentOrElse(
        value -> {
          if (isBooleanFalse(value)) {
            ctx.reportIssue(value, DISABLED_CERTIFICATE);
          }
        },
        () -> ctx.reportIssue(resource.type(), String.format(MISSING_CERTIFICATE, "negotiateClientCertificate")));
  }

  private static boolean isBooleanFalse(Tree tree) {
    return ((ArmTree) tree).is(ArmTree.Kind.BOOLEAN_LITERAL) && !((BooleanLiteral) tree).value();
  }
}
