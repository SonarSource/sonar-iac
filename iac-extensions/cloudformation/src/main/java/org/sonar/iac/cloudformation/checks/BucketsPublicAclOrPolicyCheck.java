/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6281")
public class BucketsPublicAclOrPolicyCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure not preventing permissive ACL/policies to be set is safe here.";
  private static final String SECONDARY_MSG_PROPERTY = "Set this property to true";
  private static final String SECONDARY_MSG_BUCKET = "Related bucket";
  private static final List<String> ATTRIBUTES_TO_CHECK = Arrays.asList(
    "BlockPublicAcls",
    "BlockPublicPolicy",
    "IgnorePublicAcls",
    "RestrictPublicBuckets");

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!isS3Bucket(resource)) {
      return;
    }

    Optional<CloudformationTree> accessConfiguration = PropertyUtils.value(resource.properties(), "PublicAccessBlockConfiguration", CloudformationTree.class);
    if (accessConfiguration.isPresent()) {
      checkConfiguration(ctx, resource, accessConfiguration.get());
    } else {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static void checkConfiguration(CheckContext ctx, Resource resource, CloudformationTree configuration) {
    List<SecondaryLocation> problemsAsSecondaryLocations = configurationProblemsAsSecondaryLocations(configuration);
    Tree primaryLocationTree = PropertyUtils.key(resource.properties(), "PublicAccessBlockConfiguration").orElse(configuration);

    if (!problemsAsSecondaryLocations.isEmpty() || hasMissingSetting(configuration)) {
      problemsAsSecondaryLocations.add(new SecondaryLocation(resource.type(), SECONDARY_MSG_BUCKET));
      ctx.reportIssue(primaryLocationTree, MESSAGE, problemsAsSecondaryLocations);
    }
  }

  private static boolean hasMissingSetting(CloudformationTree configuration) {
    return ATTRIBUTES_TO_CHECK.stream().anyMatch(a -> PropertyUtils.has(configuration, a).isFalse());
  }

  private static List<SecondaryLocation> configurationProblemsAsSecondaryLocations(CloudformationTree configuration) {
    List<SecondaryLocation> problems = new ArrayList<>();
    ATTRIBUTES_TO_CHECK.forEach(attribute -> PropertyUtils.value(configuration, attribute)
      .filter(TextUtils::isValueFalse)
      .ifPresent(c -> problems.add(new SecondaryLocation(c, SECONDARY_MSG_PROPERTY))));
    return problems;
  }
}
