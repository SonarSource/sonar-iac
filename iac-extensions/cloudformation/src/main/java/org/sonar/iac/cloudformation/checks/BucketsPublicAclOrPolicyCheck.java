/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6281")
public class BucketsPublicAclOrPolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure allowing public ACL/policies to be set is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"PublicAccessBlockConfiguration\" allows public ACL/policies to be set on this S3 bucket. Make sure it is safe here.";
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

    Optional<YamlTree> accessConfiguration = PropertyUtils.value(resource.properties(), "PublicAccessBlockConfiguration", YamlTree.class);
    if (accessConfiguration.isPresent()) {
      checkConfiguration(ctx, resource, accessConfiguration.get());
    } else {
      ctx.reportIssue(resource.type(), OMITTING_MESSAGE);
    }
  }

  private static void checkConfiguration(CheckContext ctx, Resource resource, YamlTree configuration) {
    List<SecondaryLocation> problemsAsSecondaryLocations = configurationProblemsAsSecondaryLocations(configuration);
    Tree primaryLocationTree = PropertyUtils.key(resource.properties(), "PublicAccessBlockConfiguration").orElse(configuration);

    if (!problemsAsSecondaryLocations.isEmpty() || hasMissingSetting(configuration)) {
      problemsAsSecondaryLocations.add(new SecondaryLocation(resource.type(), SECONDARY_MSG_BUCKET));
      ctx.reportIssue(primaryLocationTree, MESSAGE, problemsAsSecondaryLocations);
    }
  }

  private static boolean hasMissingSetting(YamlTree configuration) {
    return ATTRIBUTES_TO_CHECK.stream().anyMatch(a -> PropertyUtils.isMissing(configuration, a));
  }

  private static List<SecondaryLocation> configurationProblemsAsSecondaryLocations(YamlTree configuration) {
    List<SecondaryLocation> problems = new ArrayList<>();
    ATTRIBUTES_TO_CHECK.forEach(attribute -> PropertyUtils.value(configuration, attribute)
      .filter(TextUtils::isValueFalse)
      .ifPresent(c -> problems.add(new SecondaryLocation(c, SECONDARY_MSG_PROPERTY))));
    return problems;
  }
}
