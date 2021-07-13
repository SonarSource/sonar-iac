/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6281")
public class BucketsPublicAclOrPolicyCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure allowing public policy/acl access is safe here.";
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

    Optional<CloudformationTree> accessConfiguration = MappingTreeUtils.getValue(resource.properties(), "PublicAccessBlockConfiguration");
    if (accessConfiguration.isPresent()) {
      checkConfiguration(ctx, resource, accessConfiguration.get());
    } else {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static void checkConfiguration(CheckContext ctx, Resource resource, CloudformationTree configuration) {
    List<SecondaryLocation> problemsAsSecondaryLocations = configurationProblemsAsSecondaryLocations(configuration);
    CloudformationTree primaryLocationTree = getKey(resource.properties()).orElse(configuration);

    if (!problemsAsSecondaryLocations.isEmpty() || hasMissingSetting(configuration)) {
      problemsAsSecondaryLocations.add(new SecondaryLocation(resource.type(), SECONDARY_MSG_BUCKET));
      ctx.reportIssue(primaryLocationTree, MESSAGE, problemsAsSecondaryLocations);
    }
  }

  private static boolean hasMissingSetting(CloudformationTree configuration) {
    return ATTRIBUTES_TO_CHECK.stream().anyMatch(a -> MappingTreeUtils.hasValue(configuration, a).isFalse());
  }

  private static List<SecondaryLocation> configurationProblemsAsSecondaryLocations(CloudformationTree configuration) {
    List<SecondaryLocation> problems = new ArrayList<>();
    ATTRIBUTES_TO_CHECK.forEach(attribute -> MappingTreeUtils.getValue(configuration, attribute)
      .filter(TextUtils::isValueFalse)
      .ifPresent(c -> problems.add(new SecondaryLocation(c, SECONDARY_MSG_PROPERTY))));
    return problems;
  }

  private static Optional<CloudformationTree> getKey(CloudformationTree properties) {
    return ((MappingTree) properties).elements().stream()
      .map(TupleTree::key)
      .filter(k -> k instanceof ScalarTree && "PublicAccessBlockConfiguration".equals(((ScalarTree) k).value()))
      .findFirst();
  }
}
