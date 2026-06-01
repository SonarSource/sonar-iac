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
package org.sonar.iac.arm.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualMap;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isArrayWithValues;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isEmptyArray;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isTrue;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isValue;
import static org.sonar.iac.arm.checks.utils.CheckUtils.skipReferencingResources;

@Rule(key = "S6382")
public class CertificateBasedAuthenticationCheck extends AbstractArmResourceCheck {

  private static final String CLIENT_CERTIFICATE_ENABLED_PROPERTY = "clientCertEnabled";

  private static final String MESSAGE_ENABLE_CERT_AUTH = "Enable client certificate authentication for this resource.";
  private static final String MESSAGE_REQUIRE_CLIENT_CERTS = "Require client certificates for this resource.";
  private static final String MESSAGE_SET_CERT_PROPERTY = "Set \"%s\" to enable client certificate authentication.";
  private static final String MESSAGE_USE_CERT_AUTH = "Use client certificate authentication for this resource.";
  private static final String MESSAGE_PROVIDE_CERT_LIST = "Provide a list of certificates to enable client certificate authentication.";

  private static final Set<String> SENSITIVE_LINKED_SERVICES_TYPE = Set.of("Web", "HttpServer");
  private static final Set<String> SENSITIVE_PIPELINES_TYPE = Set.of("WebActivity", "WebHook");
  private static final Set<String> SENSITIVE_PIPELINES_AUTHENTICATION_TYPE = Set.of("Basic", "ServicePrincipal");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.ApiManagement/service/gateways/hostnameConfigurations", resource -> resource.property("negotiateClientCertificate")
      .reportIf(isFalse(), MESSAGE_ENABLE_CERT_AUTH)
      .reportIfAbsent(MESSAGE_SET_CERT_PROPERTY));
    register("Microsoft.App/containerApps", CertificateBasedAuthenticationCheck::checkContainerApps);
    register("Microsoft.ContainerRegistry/registries/tokens", CertificateBasedAuthenticationCheck::checkRegistriesTokens);
    register("Microsoft.DataFactory/factories/linkedservices", CertificateBasedAuthenticationCheck::checkLinkedServices);
    register("Microsoft.DocumentDB/cassandraClusters", CertificateBasedAuthenticationCheck::checkCassandraClusters);
    register("Microsoft.ServiceFabric/clusters", skipReferencingResources(CertificateBasedAuthenticationCheck::checkServiceFabric));
    register("Microsoft.Web/sites", CertificateBasedAuthenticationCheck::checkWebSites);
    register("Microsoft.Web/sites/slots", CertificateBasedAuthenticationCheck::checkWebSitesSlots);
    register("Microsoft.DataFactory/factories/pipelines", CertificateBasedAuthenticationCheck::checkPipelines);
    register(List.of("Microsoft.SignalRService/signalR", "Microsoft.SignalRService/webPubSub"), CertificateBasedAuthenticationCheck::checkSignalRService);
  }

  private static boolean isPublicNetworkAccessDisabled(ContextualResource resource) {
    return resource.property("publicNetworkAccess").is(isValue("Disabled"::equals));
  }

  private static void checkContainerApps(ContextualResource resource) {
    if (!isResourceVersionEqualsOrAfter(resource, "2022-10-01")) {
      return;
    }
    ContextualMap<ContextualObject, ObjectExpression> ingress = resource.object("configuration").object("ingress");
    if (!ingress.isPresent()) {
      return;
    }
    // external: true means the ingress is reachable beyond the Container App Environment — out of scope.
    ContextualProperty external = ingress.property("external");
    if (external.isPresent() && external.is(isTrue())) {
      return;
    }
    ingress.property("clientCertificateMode")
      .reportIf(isValue("accept"::equals), MESSAGE_REQUIRE_CLIENT_CERTS)
      .reportIf(isValue("ignore"::equals), MESSAGE_ENABLE_CERT_AUTH)
      .reportIfAbsent(MESSAGE_SET_CERT_PROPERTY);
  }

  private static void checkRegistriesTokens(ContextualResource resource) {
    ContextualMap<ContextualObject, ObjectExpression> credentials = resource.object("credentials");
    if (credentials.isPresent()) {
      credentials.property("certificates")
        .reportIf(isEmptyArray(), MESSAGE_PROVIDE_CERT_LIST)
        .reportIfAbsent(MESSAGE_SET_CERT_PROPERTY);
      credentials.property("passwords")
        .reportIf(isArrayWithValues(), MESSAGE_USE_CERT_AUTH);
    }
  }

  private static void checkLinkedServices(ContextualResource resource) {
    ContextualProperty type = resource.property("type");
    ContextualProperty authenticationType = resource.object("typeProperties").property("authenticationType");

    if (type.is(isValue(SENSITIVE_LINKED_SERVICES_TYPE::contains))
      && authenticationType.is(isValue(str -> !"ClientCertificate".equals(str)))) {
      authenticationType.report(MESSAGE_USE_CERT_AUTH, type.toSecondary("Service type"));
    }
  }

  private static void checkCassandraClusters(ContextualResource resource) {
    resource.property("clientCertificates")
      .reportIf(isEmptyArray(), MESSAGE_PROVIDE_CERT_LIST)
      .reportIfAbsent(MESSAGE_SET_CERT_PROPERTY);
  }

  private static void checkServiceFabric(ContextualResource resource) {
    ContextualProperty clientCertificateCommonNames = resource.property("clientCertificateCommonNames");
    ContextualProperty clientCertificateThumbprints = resource.property("clientCertificateThumbprints");

    if (clientCertificateCommonNames.isAbsent() && clientCertificateThumbprints.isAbsent()) {
      resource.report(String.format(MESSAGE_SET_CERT_PROPERTY, "clientCertificateCommonNames/clientCertificateThumbprints"));
    } else {
      boolean isCommonNamesCertProvided = clientCertificateCommonNames.isPresent() && clientCertificateCommonNames.is(isArrayWithValues());
      boolean isThumbprintsCertProvided = clientCertificateThumbprints.isPresent() && clientCertificateThumbprints.is(isArrayWithValues());
      if (!isCommonNamesCertProvided && !isThumbprintsCertProvided) {
        List<SecondaryLocation> secondaries = new ArrayList<>();
        clientCertificateCommonNames.ifPresent(tree -> secondaries.add(clientCertificateCommonNames.toSecondary("Empty certificate list")));
        clientCertificateThumbprints.ifPresent(tree -> secondaries.add(clientCertificateThumbprints.toSecondary("Empty certificate list")));
        resource.report(MESSAGE_PROVIDE_CERT_LIST, secondaries);
      }
    }
  }

  private static void checkWebSites(ContextualResource resource) {
    if (!isPublicNetworkAccessDisabled(resource)) {
      return;
    }
    resource.property(CLIENT_CERTIFICATE_ENABLED_PROPERTY)
      .reportIf(isFalse(), MESSAGE_ENABLE_CERT_AUTH)
      .reportIfAbsent(MESSAGE_SET_CERT_PROPERTY);
    resource.property("clientCertMode")
      .reportIf(isValue(str -> !"Required".equals(str)), MESSAGE_REQUIRE_CLIENT_CERTS)
      .reportIfAbsent(MESSAGE_SET_CERT_PROPERTY);
  }

  private static void checkWebSitesSlots(ContextualResource resource) {
    if (!isPublicNetworkAccessDisabled(resource)) {
      return;
    }
    resource.property(CLIENT_CERTIFICATE_ENABLED_PROPERTY)
      .reportIf(isFalse(), MESSAGE_ENABLE_CERT_AUTH);
    resource.property("clientCertMode")
      .reportIf(isValue(str -> !"Required".equals(str)), MESSAGE_REQUIRE_CLIENT_CERTS);
  }

  private static void checkPipelines(ContextualResource resource) {
    resource.list("activities").objects().forEach(activity -> {
      ContextualProperty type = activity.property("type");
      ContextualProperty authenticationType = activity.object("typeProperties").property("authenticationType");

      if (type.is(isValue(SENSITIVE_PIPELINES_TYPE::contains))
        && authenticationType.is(isValue(SENSITIVE_PIPELINES_AUTHENTICATION_TYPE::contains))) {
        authenticationType.report(MESSAGE_USE_CERT_AUTH, type.toSecondary("Pipeline type"));
      }
    });
  }

  private static void checkSignalRService(ContextualResource resource) {
    if (!isPublicNetworkAccessDisabled(resource)) {
      return;
    }
    resource.object("tls").property(CLIENT_CERTIFICATE_ENABLED_PROPERTY)
      .reportIf(isFalse(), MESSAGE_ENABLE_CERT_AUTH)
      .reportIfAbsent(MESSAGE_SET_CERT_PROPERTY);
  }

  private static boolean isResourceVersionEqualsOrAfter(ContextualResource resource, String version) {
    var versionString = Optional.ofNullable(resource.version)
      .filter(StringLiteral.class::isInstance)
      .map(v -> ((StringLiteral) v).value())
      .orElse("");
    return versionString.compareTo(version) >= 0;
  }
}
