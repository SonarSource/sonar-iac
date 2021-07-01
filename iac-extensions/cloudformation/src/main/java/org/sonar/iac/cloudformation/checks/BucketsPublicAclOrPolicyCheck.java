/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.cloudformation.checks.utils.ScalarTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;

@Rule(key = "S6281")
public class BucketsPublicAclOrPolicyCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure allowing public policy/acl access is safe here.";
  private static final Map<String, String> ATTRIBUTE_TO_MESSAGE = new HashMap<>();
  static {
    ATTRIBUTE_TO_MESSAGE.put("BlockPublicAcls", "Public ACLs are allowed.");
    ATTRIBUTE_TO_MESSAGE.put("BlockPublicPolicy", "Public Policies are allowed.");
    ATTRIBUTE_TO_MESSAGE.put("IgnorePublicAcls", "Public ACLs are not ignored.");
    ATTRIBUTE_TO_MESSAGE.put("RestrictPublicBuckets", "Public Buckets are not restricted.");
  }

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
    List<SecondaryLocation> problems = configurationProblems(configuration);
    if (!problems.isEmpty()) {
      ctx.reportIssue(resource.type(), MESSAGE, problems);
    } else if (configuration instanceof MappingTree && notAllConfigurationsSet(configuration)) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static List<SecondaryLocation> configurationProblems(CloudformationTree configuration) {
    List<SecondaryLocation> problems = new ArrayList<>();
    ATTRIBUTE_TO_MESSAGE.forEach((attribute, message) -> MappingTreeUtils.getValue(configuration, attribute)
      .filter(c -> ScalarTreeUtils.isValue(c, "false"))
      .ifPresent(c -> problems.add(new SecondaryLocation(c, message))));
    return problems;
  }

  private static boolean notAllConfigurationsSet(CloudformationTree configuration) {
    for (String attribute : ATTRIBUTE_TO_MESSAGE.keySet()) {
      if (!MappingTreeUtils.getValue(configuration, attribute).isPresent()) {
        return true;
      }
    }
    return false;
  }
}
