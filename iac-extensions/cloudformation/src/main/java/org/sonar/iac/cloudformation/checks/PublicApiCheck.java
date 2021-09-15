/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6333")
public class PublicApiCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure creating a public API is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::ApiGateway::Method")) {
      checkApiGatewayMethod(ctx, resource);
    } else if (resource.isType("AWS::Serverless::Api")) {
      checkServerlessApi(ctx, resource);
    } else if (resource.isType("AWS::Serverless::HttpApi")) {
      checkServerlessHttpApi(ctx, resource);
    }
  }

  private static void checkApiGatewayMethod(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), "AuthorizationType")
      .filter(typeTree -> TextUtils.isValue(typeTree, "NONE").isTrue())
      .ifPresent(typeTree -> ctx.reportIssue(typeTree, MESSAGE, new SecondaryLocation(resource.type(), "Related method")));
  }

  private static void checkServerlessApi(CheckContext ctx, Resource resource) {
    Optional<Tree> optionalAuthProperty = PropertyUtils.value(resource.properties(), "Auth");
    if (!optionalAuthProperty.isPresent()) {
      ctx.reportIssue(resource.type(), MESSAGE);
      return;
    }

    Tree authProperty = optionalAuthProperty.get();
    Optional<Tree> keyRequirementProperty = PropertyUtils.value(authProperty, "ApiKeyRequired");
    if (keyRequirementProperty.isPresent() && TextUtils.isValueFalse(keyRequirementProperty.get()) && noRequiredAuthPropertySet(authProperty)) {
      ctx.reportIssue(keyRequirementProperty.get(), MESSAGE, new SecondaryLocation(resource.type(), "Related API"));
    }
  }

  private static void checkServerlessHttpApi(CheckContext ctx, Resource resource) {
    Optional<Tree> optionalAuthProperty = PropertyUtils.value(resource.properties(), "Auth");
    if (!optionalAuthProperty.isPresent()) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static boolean noRequiredAuthPropertySet(Tree authProperty) {
    return Stream.of("ResourcePolicy", "Authorizers", "DefaultAuthorizer")
      .noneMatch(p -> PropertyUtils.has(authProperty, p).isTrue());
  }
}
