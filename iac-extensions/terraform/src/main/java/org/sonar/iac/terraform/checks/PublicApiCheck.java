/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;

import static org.sonar.iac.common.checks.PropertyUtils.value;
import static org.sonar.iac.common.checks.TextUtils.isValue;

@Rule(key = "S6333")
public class PublicApiCheck extends AbstractCrossResourceCheck {

  private static final String MESSAGE = "Make sure creating a public API is safe here.";

  @Override
  protected void registerResourceChecks() {
    register(PublicApiCheck::checkApiGatewayMethod, "aws_api_gateway_method");
    register(this::checkApiGatewayV2Route, "aws_apigatewayv2_route");
  }

  private static void checkApiGatewayMethod(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "authorization", AttributeTree.class)
      .ifPresent(authorization -> reportSensitiveValue(ctx, authorization, "NONE", MESSAGE,
        new SecondaryLocation(resource.labels().get(0), "Related method")));
  }

  private void checkApiGatewayV2Route(CheckContext ctx, BlockTree resource) {
    var authorizationTypeNoneOrAbsent = PropertyUtils.get(resource, "authorization_type", AttributeTree.class)
      .filter(authType -> !isValue(authType.value(), "NONE").isTrue())
      .isEmpty();
    if (!authorizationTypeNoneOrAbsent) {
      return;
    }

    var relatedProtocolType = PropertyUtils.get(resource, "api_id", AttributeTree.class)
      .map((AttributeTree apiId) -> {
        var resourceName = ((AttributeAccessTree) ((AttributeAccessTree) apiId.value()).object()).attribute().value();
        return blockNameToBlockTree.get(resourceName);
      })
      .flatMap(blockTree -> value(blockTree, "protocol_type", LiteralExprTree.class));

    var primaryLocation = PropertyUtils.get(resource, "authorization_type", AttributeTree.class)
      .filter(authType -> isValue(authType.value(), "NONE").isTrue())
      .map(TerraformTree.class::cast)
      .orElse(resource);

    boolean isHttp = relatedProtocolType.map(protocolType -> isValue(protocolType, "HTTP").isTrue()).orElse(false);
    if (isHttp) {
      var secondaryLocations = List.of(new SecondaryLocation(relatedProtocolType.get(), "Related API"));
      reportTreeOrResource(primaryLocation, ctx, MESSAGE, secondaryLocations);
    }

    boolean isWebsocket = relatedProtocolType.map(protocolType -> isValue(protocolType, "WEBSOCKET").isTrue()).orElse(false);
    if (isWebsocket) {
      var routeKey = value(resource, "route_key", LiteralExprTree.class)
        .filter(routeKeyTree -> isValue(routeKeyTree, "$connect").isTrue());

      if (routeKey.isPresent()) {
        var secondaryLocations = List.of(
          new SecondaryLocation(routeKey.get(), "Related route_key"),
          new SecondaryLocation(relatedProtocolType.get(), "Related API"));
        reportTreeOrResource(primaryLocation, ctx, MESSAGE, secondaryLocations);
      }
    }
  }

  private static void reportTreeOrResource(TerraformTree tree, CheckContext ctx, String message, List<SecondaryLocation> secondaries) {
    if (tree instanceof BlockTree blockTree) {
      reportResource(ctx, blockTree, message, secondaries);
    } else {
      ctx.reportIssue(tree, message, secondaries);
    }
  }
}
