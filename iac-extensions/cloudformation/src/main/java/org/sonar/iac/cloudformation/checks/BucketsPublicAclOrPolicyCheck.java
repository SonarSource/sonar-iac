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

  private static final String MESSAGE = "Disabling public access block settings allows public ACL/policies to be set on this S3 bucket.";
  private static final String MISSING_MESSAGE = "Omitting a public access block setting defaults it to false, allowing public ACL/policies to be set on this S3 bucket.";
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
    accessConfiguration.ifPresent(configuration -> checkConfiguration(ctx, resource, configuration));
  }

  private static void checkConfiguration(CheckContext ctx, Resource resource, YamlTree configuration) {
    List<SecondaryLocation> problemsAsSecondaryLocations = configurationProblemsAsSecondaryLocations(configuration);
    Tree primaryLocationTree = PropertyUtils.key(resource.properties(), "PublicAccessBlockConfiguration").orElse(configuration);

    if (!problemsAsSecondaryLocations.isEmpty()) {
      problemsAsSecondaryLocations.add(new SecondaryLocation(resource.type(), SECONDARY_MSG_BUCKET));
      ctx.reportIssue(primaryLocationTree, MESSAGE, problemsAsSecondaryLocations);
    } else if (hasMissingSetting(configuration)) {
      ctx.reportIssue(primaryLocationTree, MISSING_MESSAGE, List.of(new SecondaryLocation(resource.type(), SECONDARY_MSG_BUCKET)));
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
