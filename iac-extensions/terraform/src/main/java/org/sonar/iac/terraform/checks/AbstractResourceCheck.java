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
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.CheckForNull;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

public abstract class AbstractResourceCheck implements IacCheck {

  public static final String S3_BUCKET = "aws_s3_bucket";
  private final Map<String, List<BiConsumer<CheckContext, BlockTree>>> resourceChecks = new HashMap<>();
  private final List<BiConsumer<CheckContext, BlockTree>> allResourcesChecks = new ArrayList<>();

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, (ctx, tree) -> {
      if (isResource(tree)) {
        checkResource(ctx, tree);
      }
    });
    registerResourceChecks();
  }

  protected void registerResourceChecks() {
    // do not register any check for a specific resource type by default
  }

  protected void register(BiConsumer<CheckContext, BlockTree> resourceCheck) {
    allResourcesChecks.add(resourceCheck);
  }

  protected void register(BiConsumer<CheckContext, BlockTree> resourceCheck, String... resourceNames) {
    Arrays.asList(resourceNames).forEach(resourceName -> resourceChecks.computeIfAbsent(resourceName, i -> new ArrayList<>()).add(resourceCheck));
  }

  protected void checkResource(CheckContext ctx, BlockTree resource) {
    String resourceType = getResourceType(resource);
    if (resourceChecks.containsKey(resourceType)) {
      resourceChecks.get(resourceType).forEach(consumer -> consumer.accept(ctx, resource));
    }
    allResourcesChecks.forEach(consumer -> consumer.accept(ctx, resource));
  }

  public static boolean isResource(BlockTree tree) {
    return TextUtils.matchesValue(tree.key(), "resource"::equals).isTrue();
  }

  public static boolean isResource(BlockTree tree, String type) {
    return isResource(tree) && type.equals(getResourceType(tree));
  }

  public static String getReferenceLabel(BlockTree resource) {
    return resource.labels().get(1).value();
  }

  public static boolean hasReferenceLabel(BlockTree resource) {
    return resource.labels().size() >= 2;
  }

  public static boolean isS3Bucket(BlockTree tree) {
    return S3_BUCKET.equals(getResourceType(tree));
  }

  public static boolean isS3BucketResource(BlockTree tree) {
    return isResource(tree, S3_BUCKET);
  }

  @CheckForNull
  public static String getResourceType(BlockTree tree) {
    return tree.labels().isEmpty() ? null : tree.labels().get(0).value();
  }

  public static void reportResource(CheckContext ctx, BlockTree resource, String message) {
    ctx.reportIssue(resource.labels().get(0), message);
  }

  protected static void reportOnTrue(CheckContext ctx, AttributeTree attribute, String message, SecondaryLocation... secondaries) {
    if (TextUtils.isValueTrue(attribute.value())) {
      ctx.reportIssue(attribute, message, Arrays.asList(secondaries));
    }
  }

  protected static void reportOnFalse(CheckContext ctx, AttributeTree attribute, String message, SecondaryLocation... secondaries) {
    if (TextUtils.isValueFalse(attribute.value())) {
      ctx.reportIssue(attribute, message, Arrays.asList(secondaries));
    }
  }

  protected static void reportUnexpectedValue(CheckContext ctx, AttributeTree attribute, String expectedValue, String message, SecondaryLocation... secondaries) {
    if (TextUtils.isValue(attribute.value(), expectedValue).isFalse()) {
      ctx.reportIssue(attribute, message, Arrays.asList(secondaries));
    }
  }

  protected static void reportSensitiveValue(CheckContext ctx, AttributeTree attribute, String sensitiveValue, String message, SecondaryLocation... secondaries) {
    if (TextUtils.isValue(attribute.value(), sensitiveValue).isTrue()) {
      ctx.reportIssue(attribute, message, Arrays.asList(secondaries));
    }
  }
}
