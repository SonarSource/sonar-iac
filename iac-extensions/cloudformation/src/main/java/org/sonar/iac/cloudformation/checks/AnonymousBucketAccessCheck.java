/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.checks.utils.ScalarTreeUtils;
import org.sonar.iac.cloudformation.checks.utils.XPathUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;

@Rule(key = "S6270")
public class AnonymousBucketAccessCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure this S3 policy granting anonymous access is safe here.";
  private static final String SECONDARY_MSG = "Anonymous access.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::S3::BucketPolicy") && isAllowingPolicy(resource.properties())) {
      // Due to the fact that for now secondary locations are not supported we only raise a single issue if one principal allows anonymous access
      anonymousPrincipal(resource.properties())
        .ifPresent(p -> ctx.reportIssue(resource.type(), MESSAGE, new SecondaryLocation(p, SECONDARY_MSG)));
    }
  }

  private static boolean isAllowingPolicy(CloudformationTree properties) {
    CloudformationTree effect = XPathUtils.getSingleTree(properties, "/PolicyDocument/Statement[]/Effect").orElse(null);
    return ScalarTreeUtils.isValue(effect, "Allow");
  }

  private static Optional<CloudformationTree> anonymousPrincipal(CloudformationTree properties) {
    Optional<CloudformationTree> principal = XPathUtils.getSingleTree(properties, "/PolicyDocument/Statement[]/Principal");
    if (principal.isPresent() && principal.get() instanceof MappingTree) {
      return identifyPrincipalWithWildcardRule((MappingTree) principal.get());
    }
    return Optional.empty();
  }

  private static Optional<CloudformationTree> identifyPrincipalWithWildcardRule(MappingTree tree) {
    for (TupleTree tuple: tree.elements()) {
      CloudformationTree principalWithWildcard = isWildcardRule(tuple.value());
      if (principalWithWildcard != null) return Optional.of(principalWithWildcard);
    }
    return Optional.empty();
  }

  @CheckForNull
  private static CloudformationTree isWildcardRule(CloudformationTree principalRule) {
    if (principalRule instanceof SequenceTree) {
      return ((SequenceTree) principalRule).elements().stream()
        .map(AnonymousBucketAccessCheck::isWildcardRule).filter(Objects::nonNull).findFirst().orElse(null);
    }
    return ScalarTreeUtils.isValue(principalRule, "*") ? principalRule : null;
  }
}
