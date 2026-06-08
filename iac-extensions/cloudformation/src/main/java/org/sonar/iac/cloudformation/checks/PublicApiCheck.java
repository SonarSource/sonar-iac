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
package org.sonar.iac.cloudformation.checks;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.tree.FunctionCallTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.sonar.iac.common.checks.PropertyUtils.value;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.extractMethodFromRouteKey;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.hasSensitiveName;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.hasSensitiveRouteKeyValue;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.isBootstrapName;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.isDangerousMethod;
import static org.sonar.iac.common.checks.TextUtils.isValue;

@Rule(key = "S6333")
public class PublicApiCheck extends AbstractCrossResourceCheck {

  private static final String MESSAGE = "Make sure creating a public API is safe here.";
  private static final String RELATED_METHOD_MESSAGE = "Related method";
  private static final String RELATED_API_MESSAGE = "Related API";

  private static final String ROUTE_KEY = "RouteKey";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::ApiGateway::Method")) {
      checkApiGatewayMethod(ctx, resource);
    } else if (resource.isType("AWS::Serverless::Api")) {
      checkServerlessApi(ctx, resource);
    } else if (resource.isType("AWS::Serverless::HttpApi")) {
      checkServerlessHttpApi(ctx, resource);
    } else if (resource.isType("AWS::ApiGatewayV2::Route")) {
      checkApiGatewayV2Api(ctx, resource);
    }
  }

  private void checkApiGatewayV2Api(CheckContext ctx, Resource resource) {
    checkAuthorizationTypeIsNone(resource)
      .ifPresent((Tree routeTree) -> {
        if (isBootstrapName(resource.name().value()) && !hasSensitiveName(resource.name().value())) {
          return;
        }

        var apiIdRefs = getApiIdRefs(resource);
        apiIdRefs.ifPresent(refList -> checkReferencedResourceForProtocolTypeHttp(ctx, resource, routeTree, refList));

        value(resource.properties(), ROUTE_KEY)
          .filter(routeKey -> isValue(routeKey, "$connect").isTrue())
          .ifPresent(routeKey -> apiIdRefs
            .ifPresent(refList -> checkReferencedResourceForProtocolTypeWebsocket(ctx, routeTree, refList, routeKey)));
      });
  }

  private static Optional<List<YamlTree>> getApiIdRefs(Resource resource) {
    return value(resource.properties(), "ApiId", FunctionCallTree.class)
      .filter(tree -> "Ref".equals(tree.name()))
      .map(FunctionCallTree::arguments);
  }

  private static Optional<Tree> checkAuthorizationTypeIsNone(Resource resource) {
    return value(resource.properties(), "AuthorizationType")
      .filter(routeTree -> isValue(routeTree, "NONE").isTrue());
  }

  private void checkReferencedResourceForProtocolTypeHttp(CheckContext ctx, Resource route, Tree routeTree, List<YamlTree> refList) {
    String resourceName = route.name().value();
    checkReferencedResourceForProtocolType(refList, "HTTP")
      .forEach(protocolType -> {
        Optional<String> method = extractRouteKeyMethod(route);
        if (method.isPresent()) {
          String upperMethod = method.get().toUpperCase(Locale.ROOT);
          if (isDangerousMethod(upperMethod) || isSensitiveResource(resourceName, route)) {
            ctx.reportIssue(routeTree, MESSAGE, new SecondaryLocation(protocolType, RELATED_API_MESSAGE));
          }
        } else if (isSensitiveResource(resourceName, route)) {
          ctx.reportIssue(routeTree, MESSAGE, new SecondaryLocation(protocolType, RELATED_API_MESSAGE));
        }
      });
  }

  private static boolean isSensitiveResource(String resourceName, Resource route) {
    return hasSensitiveName(resourceName) || hasSensitiveRouteKey(route);
  }

  private static boolean hasSensitiveRouteKey(Resource route) {
    return value(route.properties(), ROUTE_KEY)
      .flatMap(TextUtils::getValue)
      .map(key -> hasSensitiveRouteKeyValue(key))
      .orElse(false);
  }

  private static Optional<String> extractRouteKeyMethod(Resource route) {
    return value(route.properties(), ROUTE_KEY)
      .flatMap(TextUtils::getValue)
      .flatMap(key -> extractMethodFromRouteKey(key));
  }

  private void checkReferencedResourceForProtocolTypeWebsocket(CheckContext ctx, Tree routeTree, List<YamlTree> refList, Tree routeKey) {
    checkReferencedResourceForProtocolType(refList, "WEBSOCKET")
      .forEach(protocolType -> ctx.reportIssue(routeTree, MESSAGE, List.of(
        new SecondaryLocation(routeKey, "Related RouteKey"),
        new SecondaryLocation(protocolType, RELATED_API_MESSAGE))));
  }

  private Stream<Tree> checkReferencedResourceForProtocolType(List<YamlTree> refList, String protocol) {
    return getResourcesByRefs(refList)
      .map(resource -> value(resource.properties(), "ProtocolType")
        .filter(protocolTypeTree -> isValue(protocolTypeTree, protocol).isTrue()))
      .filter(Optional::isPresent)
      .map(Optional::get);
  }

  private Stream<Resource> getResourcesByRefs(List<YamlTree> refList) {
    return refList.stream()
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(scalarTree -> resourceNameToResource.get(scalarTree.value()))
      .filter(Objects::nonNull);
  }

  private static void checkApiGatewayMethod(CheckContext ctx, Resource resource) {
    checkAuthorizationTypeIsNone(resource).ifPresent(typeTree -> {
      String name = resource.name().value();
      if (isBootstrapName(name) && !hasSensitiveName(name)) {
        return;
      }

      Optional<String> httpMethod = value(resource.properties(), "HttpMethod")
        .flatMap(TextUtils::getValue);

      if (httpMethod.isPresent()) {
        String method = httpMethod.get().toUpperCase(Locale.ROOT);
        if (isDangerousMethod(method) || hasSensitiveName(name)) {
          ctx.reportIssue(typeTree, MESSAGE, new SecondaryLocation(resource.type(), RELATED_METHOD_MESSAGE));
        }
      } else if (hasSensitiveName(name)) {
        ctx.reportIssue(typeTree, MESSAGE, new SecondaryLocation(resource.type(), RELATED_METHOD_MESSAGE));
      }
    });
  }

  private static boolean isPrivateEndpoint(Resource resource) {
    return value(resource.properties(), "EndpointConfiguration")
      .map(ec -> {
        // SAM style: EndpointConfiguration: Type: PRIVATE
        if (value(ec, "Type").filter(t -> isValue(t, "PRIVATE").isTrue()).isPresent()) {
          return true;
        }
        // Native CFN style: EndpointConfiguration: Types: [PRIVATE]
        return value(ec, "Types")
          .filter(SequenceTree.class::isInstance)
          .map(SequenceTree.class::cast)
          .map(seq -> seq.elements().stream().anyMatch(item -> isValue(item, "PRIVATE").isTrue()))
          .orElse(false);
      })
      .orElse(false);
  }

  private static void checkServerlessApi(CheckContext ctx, Resource resource) {
    if (isPrivateEndpoint(resource)) {
      return;
    }

    Optional<Tree> optionalAuthProperty = value(resource.properties(), "Auth");
    if (optionalAuthProperty.isEmpty()) {
      ctx.reportIssue(resource.type(), MESSAGE);
      return;
    }

    Tree authProperty = optionalAuthProperty.get();
    Optional<Tree> keyRequirementProperty = value(authProperty, "ApiKeyRequired");
    if (keyRequirementProperty.isPresent() && TextUtils.isValueFalse(keyRequirementProperty.get()) && noRequiredAuthPropertySet(authProperty)) {
      ctx.reportIssue(keyRequirementProperty.get(), MESSAGE, new SecondaryLocation(resource.type(), RELATED_API_MESSAGE));
    }
  }

  private static void checkServerlessHttpApi(CheckContext ctx, Resource resource) {
    Optional<Tree> optionalAuthProperty = value(resource.properties(), "Auth");
    if (optionalAuthProperty.isEmpty()) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static boolean noRequiredAuthPropertySet(Tree authProperty) {
    return Stream.of("ResourcePolicy", "Authorizers", "DefaultAuthorizer")
      .noneMatch(p -> PropertyUtils.has(authProperty, p).isTrue());
  }
}
