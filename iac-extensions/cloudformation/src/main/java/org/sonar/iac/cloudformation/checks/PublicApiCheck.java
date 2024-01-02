/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
