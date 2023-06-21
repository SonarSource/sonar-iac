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
import org.sonar.iac.common.api.checks.SecondaryLocation;
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
  private static final Set<String> SENSITIVE_PIPELINES_TYPE = Set.of("WebActivity", "WebHook");
  private static final Set<String> SENSITIVE_PIPELINES_AUTHENTICATION_TYPE = Set.of("Basic", "ServicePrincipal");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.ApiManagement/service/gateways/hostnameConfigurations", resource -> resource.property("negotiateClientCertificate")
      .reportIf(isFalse(), DISABLED_CERTIFICATE_MESSAGE)
      .reportIfAbsent(MISSING_CERTIFICATE_MESSAGE));
    register("Microsoft.App/containerApps", CertificateBasedAuthenticationCheck::checkContainerApps);
    register("Microsoft.ContainerRegistry/registries/tokens", CertificateBasedAuthenticationCheck::checkRegistriesTokens);
    register("Microsoft.DataFactory/factories/linkedservices", CertificateBasedAuthenticationCheck::checkLinkedServices);
    register("Microsoft.DocumentDB/cassandraClusters", CertificateBasedAuthenticationCheck::checkCassandraClusters);
    register("Microsoft.Scheduler/jobCollections/jobs", CertificateBasedAuthenticationCheck::checkJobCollections);
    register("Microsoft.ServiceFabric/clusters", CertificateBasedAuthenticationCheck::checkServiceFabric);
    register("Microsoft.Web/sites", CertificateBasedAuthenticationCheck::checkWebSites);
    register("Microsoft.Web/sites/slots", CertificateBasedAuthenticationCheck::checkWebSitesSlots);
    register("Microsoft.DataFactory/factories/pipelines", CertificateBasedAuthenticationCheck::checkPipelines);
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
    checkRequestAuthenticationType(resource.object("action"));
    checkRequestAuthenticationType(resource.object("action").object("errorAction"));
  }

  private static void checkRequestAuthenticationType(ContextualObject action) {
    action
      .object("request")
      .object("authentication")
      .property("type")
      .reportIf(isValue(str -> !CLIENT_CERTIFICIATE_VALUE.equals(str)), WRONG_AUTHENTICATION_METHOD_MESSAGE);
  }

  private static void checkServiceFabric(ContextualResource resource) {
    ContextualProperty clientCertificateCommonNames = resource.property("clientCertificateCommonNames");
    ContextualProperty clientCertificateThumbprints = resource.property("clientCertificateThumbprints");

    if (clientCertificateCommonNames.isAbsent() && clientCertificateThumbprints.isAbsent()) {
      resource.report(String.format(NO_CERTIFICATE_LIST_MESSAGE, "clientCertificateCommonNames/clientCertificateThumbprints"));
    } else {
      boolean isCommonNamesCertProvided = clientCertificateCommonNames.isPresent() && clientCertificateCommonNames.is(isArrayWithValues());
      boolean isThumbprintsCertProvided = clientCertificateThumbprints.isPresent() && clientCertificateThumbprints.is(isArrayWithValues());
      if (!isCommonNamesCertProvided && !isThumbprintsCertProvided) {
        List<SecondaryLocation> secondaries = new ArrayList<>();
        clientCertificateCommonNames.ifPresent(tree -> secondaries.add(clientCertificateCommonNames.toSecondary("Empty certificate list")));
        clientCertificateThumbprints.ifPresent(tree -> secondaries.add(clientCertificateThumbprints.toSecondary("Empty certificate list")));
        resource.report(EMPTY_CERTIFICATE_LIST_MESSAGE, secondaries);
      }
    }
  }

  private static void checkWebSites(ContextualResource resource) {
    resource.property("clientCertEnabled")
      .reportIf(isFalse(), DISABLED_CERTIFICATE_MESSAGE)
      .reportIfAbsent(MISSING_CERTIFICATE_MESSAGE);
    resource.property("clientCertMode")
      .reportIf(isValue(str -> !"Required".equals(str)), ALLOWING_NO_CERTIFICATE_MESSAGE)
      .reportIfAbsent(MISSING_CERTIFICATE_MESSAGE);
  }

  private static void checkWebSitesSlots(ContextualResource resource) {
    resource.property("clientCertEnabled")
      .reportIf(isFalse(), DISABLED_CERTIFICATE_MESSAGE);
    resource.property("clientCertMode")
      .reportIf(isValue(str -> !"Required".equals(str)), ALLOWING_NO_CERTIFICATE_MESSAGE);
  }

  private static void checkPipelines(ContextualResource resource) {
    resource.list("activities").objects().forEach(activity -> {
      ContextualProperty type = activity.property("type");
      ContextualProperty authenticationType = activity.object("typeProperties").property("authenticationType");

      if (type.is(isValue(SENSITIVE_PIPELINES_TYPE::contains))
        && authenticationType.is(isValue(SENSITIVE_PIPELINES_AUTHENTICATION_TYPE::contains))) {
        authenticationType.report(WRONG_AUTHENTICATION_METHOD_MESSAGE, type.toSecondary("Pipeline type"));
      }
    });
  }

  private static Predicate<Expression> isFalse() {
    return expr -> expr.is(ArmTree.Kind.BOOLEAN_LITERAL) && !((BooleanLiteral) expr).value();
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
