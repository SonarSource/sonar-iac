/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.checks.utils.XPathUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6270")
public class AnonymousBucketAccessCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure this S3 policy granting anonymous access is safe here.";
  private static final String SECONDARY_MSG = "Anonymous access.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    CloudformationTree properties = resource.properties();
    if (resource.isType("AWS::S3::BucketPolicy") && properties != null&& isAllowingPolicy(properties)) {
      List<CloudformationTree> anonymousPrincipals = anonymousPrincipals(properties);
      if (!anonymousPrincipals.isEmpty()) {
        ctx.reportIssue(resource.type(), MESSAGE, secondaryLocations(anonymousPrincipals));
      }
    }
  }

  private static boolean isAllowingPolicy(CloudformationTree properties) {
    CloudformationTree effect = XPathUtils.getSingleTree(properties, "/PolicyDocument/Statement[]/Effect").orElse(null);
    return TextUtils.isValue(effect, "Allow").isTrue();
  }

  /**
   * Policies can have multiple principals which can have multiple rules defining the access level.
   * We collect every rule location within a single bucket policy to show them as secondary locations.
   */
  private static List<CloudformationTree> anonymousPrincipals(CloudformationTree properties) {
    Optional<CloudformationTree> principal = XPathUtils.getSingleTree(properties, "/PolicyDocument/Statement[]/Principal");
    if (principal.isPresent() && principal.get() instanceof MappingTree) {
      return ((MappingTree) principal.get()).elements().stream()
        .map(TupleTree::value)
        .map(AnonymousBucketAccessCheck::getWildcardRules)
        .flatMap(List::stream).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private static List<CloudformationTree> getWildcardRules(CloudformationTree principalRule) {
    List<CloudformationTree> wildcardRules = new ArrayList<>();
    if (principalRule instanceof SequenceTree) {
      ((SequenceTree) principalRule).elements().stream()
        .map(AnonymousBucketAccessCheck::getWildcardRules).forEach(wildcardRules::addAll);
    } else if (TextUtils.isValue(principalRule, "*").isTrue()) {
      wildcardRules.add(principalRule);
    }
    return wildcardRules;
  }

  private static List<SecondaryLocation> secondaryLocations(List<CloudformationTree> anonymousPrincipals) {
    return anonymousPrincipals.stream().map(s -> new SecondaryLocation(s, SECONDARY_MSG)).collect(Collectors.toList());
  }
}
