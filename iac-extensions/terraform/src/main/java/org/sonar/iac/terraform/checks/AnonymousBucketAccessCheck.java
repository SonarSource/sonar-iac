/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.api.tree.TupleTree;

@Rule(key = "S6270")
public class AnonymousBucketAccessCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure this S3 policy granting anonymous access is safe here.";
  private static final String SECONDARY_MSG = "Anonymous access.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (isResource(resource, "aws_s3_bucket_policy") || isS3Bucket(resource)) {
      // Handle policy statement if present in s3_bucket_policy or s3_bucket
      PropertyUtils.value(resource, "policy").map(Policy::from)
        // Filter resolvable and allowing policies only. Resolvable means effect and principal exist in the policy.
        .filter(policy -> policy.principal().isPresent() && policy.effect().filter(AnonymousBucketAccessCheck::isAllowingPolicy).isPresent())
        .ifPresent(policy -> {
          List<LiteralExprTree> wildcardRules = getWildcardRules(policy.principal().get());
          if (!wildcardRules.isEmpty()) {
            ctx.reportIssue(resource.labels().get(0), MESSAGE, secondaryLocations(wildcardRules));
          }
        });
    }
  }

  private static List<LiteralExprTree> getWildcardRules(ExpressionTree principal) {
    List<LiteralExprTree> wildcardRules = new ArrayList<>();
    if (TextUtils.isValue(principal, "*").isTrue()) {
      wildcardRules.add((LiteralExprTree) principal);
    } else {
      // We focus on the element only on the AWS element at the moment, if it is an object mapping.
      PropertyUtils.value(principal, "AWS", ExpressionTree.class).ifPresent(aws -> {
        if (aws.is(Kind.STRING_LITERAL)) {
          // "AWS": "*"
          wildcardRules.addAll(getWildcardRules(aws));
        } else if (aws.is(Kind.TUPLE)){
          // "AWS": ["*"]
          wildcardRules.addAll(((TupleTree) aws).elements().trees().stream()
            .map(AnonymousBucketAccessCheck::getWildcardRules)
            .flatMap(List::stream).collect(Collectors.toList()));
        }
      });
    }
    return wildcardRules;
  }

  private static List<SecondaryLocation> secondaryLocations(List<LiteralExprTree> anonymousPrincipals) {
    return anonymousPrincipals.stream().map(s -> new SecondaryLocation(s, SECONDARY_MSG)).collect(Collectors.toList());
  }

  private static boolean isAllowingPolicy(ExpressionTree effect) {
    return TextUtils.isValue(effect, "Allow").isTrue();
  }
}
