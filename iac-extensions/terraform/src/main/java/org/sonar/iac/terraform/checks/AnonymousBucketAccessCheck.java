/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Policy;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Policy.Statement;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;

@Rule(key = "S6270")
public class AnonymousBucketAccessCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure this S3 policy granting anonymous access is safe here.";
  private static final String SECONDARY_MSG = "Anonymous access.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (isResource(resource, "aws_s3_bucket_policy") || isS3Bucket(resource)) {
      // Handle policy statement if present in s3_bucket_policy or s3_bucket
      PolicyUtils.getPolicies(resource).stream().forEach(policy -> {
        if (!PolicyUtils.UNKNOWN_POLCY.equals(policy)) {
          // Filter resolvable and allowing policies only. Resolvable means effect and principal exist in the policy.
          policy.statement().stream()
            .filter(AnonymousBucketAccessCheck::isAllowEffect)
            .map(Policy.Statement::principal)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(AnonymousBucketAccessCheck::getWildcardRules)
            .filter(wildcardRules -> !wildcardRules.isEmpty())
            .forEach(wildcardRules -> ctx.reportIssue(resource.labels().get(0), MESSAGE, secondaryLocations(wildcardRules)));
        }
      });
    }
  }

  private static boolean isAllowEffect(Statement statement) {
    return TextUtils.isValue(statement.effect().orElse(null), "Allow").isTrue();
  }

  private static List<LiteralExprTree> getWildcardRules(Tree principal) {
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
}
