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

import java.util.Set;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualMap;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6382")
public class CertificateBasedAuthenticationCheck extends AbstractArmResourceCheck {

  private static final String CLIENT_CERTIFICIATE_VALUE = "ClientCertificate";

  private static final String MISSING_CERTIFICATE_MESSAGE = "Omitting \"%s\" disables certificate-based authentication. Make sure it is safe here.";
  private static final String DISABLED_CERTIFICATE_MESSAGE = "Make sure that disabling certificate-based authentication is safe here.";
  private static final String ALLOWING_NO_CERTIFICATE_MESSAGE = "Connections without client certificates will be permitted. Make sure it is safe here.";
  private static final String PASSWORD_USE_MESSAGE = "This authentication method is not certificate-based. Make sure it is safe here.";
  private static final String NO_CERTIFICATE_LIST_MESSAGE = "Omitting \"%s\" disables certificate-based authentication. Make sure it is safe here.";
  private static final String EMPTY_CERTIFICATE_LIST_MESSAGE = "Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here.";
  private static final String WRONG_AUTHENTICATION_METHOD_MESSAGE = "This authentication method is not certificate-based. Make sure it is safe here.";

  private static final Set<String> SENSITIVE_LINKED_SERVICES_TYPE = Set.of("Web", "HttpServer");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.ApiManagement/service/gateways/hostnameConfigurations", resource -> resource.property("negotiateClientCertificate")
      .reportIf(CertificateBasedAuthenticationCheck::isBooleanFalse, DISABLED_CERTIFICATE_MESSAGE)
      .reportIfAbsent(MISSING_CERTIFICATE_MESSAGE));
    register("Microsoft.App/containerApps", CertificateBasedAuthenticationCheck::checkContainerApps);
    register("Microsoft.ContainerRegistry/registries/tokens", CertificateBasedAuthenticationCheck::checkRegistriesTokens);
    register("Microsoft.DataFactory/factories/linkedservices", CertificateBasedAuthenticationCheck::checkLinkedServices);
    register("Microsoft.DocumentDB/cassandraClusters", CertificateBasedAuthenticationCheck::checkCassandraClusters);
    register("Microsoft.Scheduler/jobCollections/jobs", CertificateBasedAuthenticationCheck::checkJobCollections);
  }

  private static void checkContainerApps(ContextualResource resource) {
    if (isResourceVersionEqualsOrAfter(resource, "2022-10-01")) {
      ContextualMap<ContextualObject, ObjectExpression> ingress = resource.object("configuration").object("ingress");
      if (ingress.isPresent()) {
        ingress.property("clientCertificateMode")
          .reportIf(isValue("accept"::equals), ALLOWING_NO_CERTIFICATE_MESSAGE)
          .reportIf(isValue("ignore"::equals), DISABLED_CERTIFICATE_MESSAGE)
          .reportIfAbsent(MISSING_CERTIFICATE_MESSAGE);
      }
    }
  }

  private static void checkRegistriesTokens(ContextualResource resource) {
    ContextualMap<ContextualObject, ObjectExpression> credentials = resource.object("credentials");
    if (credentials.isPresent()) {
      credentials.property("certificates")
        .reportIf(isEmptyArray(), EMPTY_CERTIFICATE_LIST_MESSAGE)
        .reportIfAbsent(NO_CERTIFICATE_LIST_MESSAGE);
      credentials.property("passwords")
        .reportIf(isArrayWithValues(), PASSWORD_USE_MESSAGE);
    }
  }

  private static void checkLinkedServices(ContextualResource resource) {
    ContextualProperty type = resource.property("type");
    ContextualProperty authenticationType = resource.object("typeProperties").property("authenticationType");

    if (type.is(isValue(SENSITIVE_LINKED_SERVICES_TYPE::contains))
      && authenticationType.is(isValue(str -> !CLIENT_CERTIFICIATE_VALUE.equals(str)))) {
      authenticationType.report(WRONG_AUTHENTICATION_METHOD_MESSAGE, type.toSecondary("Service type"));
    }
  }

  private static void checkCassandraClusters(ContextualResource resource) {
    resource.property("clientCertificates")
      .reportIf(isEmptyArray(), EMPTY_CERTIFICATE_LIST_MESSAGE)
      .reportIfAbsent(NO_CERTIFICATE_LIST_MESSAGE);
  }

  private static void checkJobCollections(ContextualResource resource) {
    resource.object("action")
      .object("request")
      .object("authentication")
      .property("type")
      .reportIf(isValue(str -> !CLIENT_CERTIFICIATE_VALUE.equals(str)), WRONG_AUTHENTICATION_METHOD_MESSAGE);
    resource.object("action")
      .object("errorAction")
      .object("request")
      .object("authentication")
      .property("type")
      .reportIf(isValue(str -> !CLIENT_CERTIFICIATE_VALUE.equals(str)), WRONG_AUTHENTICATION_METHOD_MESSAGE);
  }

  private static boolean isBooleanFalse(Tree tree) {
    return ((ArmTree) tree).is(ArmTree.Kind.BOOLEAN_LITERAL) && !((BooleanLiteral) tree).value();
  }

  private static Predicate<Expression> isValue(Predicate<String> predicate) {
    return expr -> TextUtils.matchesValue(expr, predicate).isTrue();
  }

  private static boolean isResourceVersionEqualsOrAfter(ContextualResource resource, String version) {
    return resource.version.compareTo(version) >= 0;
  }

  private static Predicate<Expression> isArrayWithValues() {
    return expr -> expr.is(ArmTree.Kind.ARRAY_EXPRESSION) && !((ArrayExpression) expr).elements().isEmpty();
  }

  private static Predicate<Expression> isEmptyArray() {
    return expr -> expr.is(ArmTree.Kind.ARRAY_EXPRESSION) && ((ArrayExpression) expr).elements().isEmpty();
  }
}
