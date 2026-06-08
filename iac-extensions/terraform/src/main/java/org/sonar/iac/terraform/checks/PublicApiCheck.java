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
package org.sonar.iac.terraform.checks;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.common.checks.PropertyUtils.value;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.extractMethodFromRouteKey;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.hasSensitiveName;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.hasSensitiveRouteKeyValue;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.isBootstrapName;
import static org.sonar.iac.common.checks.PublicApiCheckHelper.isDangerousMethod;
import static org.sonar.iac.common.checks.TextUtils.isValue;

@Rule(key = "S6333")
public class PublicApiCheck extends AbstractNewCrossResourceCheck {

  private static final String MESSAGE = "Make sure creating a public API is safe here.";
  private static final String RELATED_METHOD_MESSAGE = "Related method";
  private static final String RELATED_API_MESSAGE = "Related API";
  private static final String ROUTE_KEY = "route_key";

  @Override
  protected void registerResourceConsumer() {
    register("aws_api_gateway_method", PublicApiCheck::checkApiGatewayMethod);
    register("aws_apigatewayv2_route", this::checkApiGatewayV2RouteResourceSymbol);
  }

  private static void checkApiGatewayMethod(ResourceSymbol resource) {
    var authAttr = PropertyUtils.get(resource.tree, "authorization", AttributeTree.class);
    if (authAttr.isEmpty() || !isValue(authAttr.get().value(), "NONE").isTrue()) {
      return;
    }

    String name = resource.name;
    if (isBootstrapName(name) && !hasSensitiveName(name)) {
      return;
    }

    var httpMethod = value(resource.tree, "http_method", LiteralExprTree.class);
    if (httpMethod.isPresent()) {
      String method = httpMethod.get().value().toUpperCase(Locale.ROOT);
      if (isDangerousMethod(method) || hasSensitiveName(name)) {
        resource.ctx.reportIssue(authAttr.get(), MESSAGE, new SecondaryLocation(resource.tree.labels().get(0), RELATED_METHOD_MESSAGE));
      }
    } else if (hasSensitiveName(name)) {
      resource.ctx.reportIssue(authAttr.get(), MESSAGE, new SecondaryLocation(resource.tree.labels().get(0), RELATED_METHOD_MESSAGE));
    }
  }

  private void checkApiGatewayV2RouteResourceSymbol(ResourceSymbol resourceSymbol) {
    checkApiGatewayV2Route(resourceSymbol.ctx, resourceSymbol.tree, resourceSymbol.name);
  }

  private void checkApiGatewayV2Route(CheckContext ctx, BlockTree resource, String resourceName) {
    var authorizationTypeNoneOrAbsent = PropertyUtils.get(resource, "authorization_type", AttributeTree.class)
      .filter(authType -> !isValue(authType.value(), "NONE").isTrue())
      .isEmpty();
    if (!authorizationTypeNoneOrAbsent) {
      return;
    }

    if (isBootstrapName(resourceName) && !hasSensitiveName(resourceName)) {
      return;
    }

    var relatedProtocolType = PropertyUtils.get(resource, "api_id", AttributeTree.class)
      .map((AttributeTree apiId) -> {
        var apiResourceName = ((AttributeAccessTree) ((AttributeAccessTree) apiId.value()).object()).attribute().value();
        return blockNameToBlockTree.get(apiResourceName);
      })
      .flatMap(blockTree -> value(blockTree, "protocol_type", LiteralExprTree.class));

    var primaryLocation = PropertyUtils.get(resource, "authorization_type", AttributeTree.class)
      .filter(authType -> isValue(authType.value(), "NONE").isTrue())
      .map(TerraformTree.class::cast)
      .orElse(resource);

    relatedProtocolType
      .filter(protocolType -> isValue(protocolType, "HTTP").isTrue())
      .ifPresent(protocolType -> {
        var routeKeyMethod = extractRouteKeyMethod(resource);
        if (routeKeyMethod.isPresent()) {
          String method = routeKeyMethod.get().toUpperCase(Locale.ROOT);
          if (isDangerousMethod(method) || isSensitiveResource(resourceName, resource)) {
            reportTreeOrResource(primaryLocation, ctx, MESSAGE, List.of(new SecondaryLocation(protocolType, RELATED_API_MESSAGE)));
          }
        } else if (isSensitiveResource(resourceName, resource)) {
          reportTreeOrResource(primaryLocation, ctx, MESSAGE, List.of(new SecondaryLocation(protocolType, RELATED_API_MESSAGE)));
        }
      });

    relatedProtocolType
      .filter(protocolType -> isValue(protocolType, "WEBSOCKET").isTrue())
      .ifPresent(protocolType -> {
        var routeKey = value(resource, ROUTE_KEY, LiteralExprTree.class)
          .filter(routeKeyTree -> isValue(routeKeyTree, "$connect").isTrue());

        if (routeKey.isPresent()) {
          reportTreeOrResource(primaryLocation, ctx, MESSAGE, List.of(
            new SecondaryLocation(routeKey.get(), "Related route_key"),
            new SecondaryLocation(protocolType, RELATED_API_MESSAGE)));
        }
      });
  }

  private static boolean isSensitiveResource(String resourceName, BlockTree resource) {
    return hasSensitiveName(resourceName) || hasSensitiveRouteKey(resource);
  }

  private static boolean hasSensitiveRouteKey(BlockTree resource) {
    return value(resource, ROUTE_KEY, LiteralExprTree.class)
      .map(LiteralExprTree::value)
      .map(key -> hasSensitiveRouteKeyValue(key))
      .orElse(false);
  }

  private static Optional<String> extractRouteKeyMethod(BlockTree resource) {
    return value(resource, ROUTE_KEY, LiteralExprTree.class)
      .map(LiteralExprTree::value)
      .flatMap(key -> extractMethodFromRouteKey(key));
  }

  private static void reportTreeOrResource(TerraformTree tree, CheckContext ctx, String message, List<SecondaryLocation> secondaries) {
    if (tree instanceof BlockTree blockTree) {
      ctx.reportIssue(blockTree.labels().get(0), message, secondaries);
    } else {
      ctx.reportIssue(tree, message, secondaries);
    }
  }
}
