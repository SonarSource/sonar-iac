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
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.BucketsInsecureHttpPolicyValidator;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TemplateExpressionTree;
import org.sonar.iac.terraform.checks.utils.PolicyUtils;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isS3BucketResource;

@Rule(key = "S6249")
public class BucketsInsecureHttpCheck implements IacCheck {

  private static final BucketsInsecureHttpPolicyValidator VALIDATOR = new BucketsInsecureHttpPolicyValidator(BucketsInsecureHttpCheck::isInsecureResource);

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      BucketsAndPoliciesCollector collector = new BucketsAndPoliciesCollector();
      collector.scan(new TreeContext(), tree);
      checkBucketsAndPolicies(ctx, bucketsToPolicies(collector.buckets, collector.policies));
    });
  }

  private static void checkBucketsAndPolicies(CheckContext ctx, Map<BlockTree, Policy> bucketsToPolicies) {
    for (Map.Entry<BlockTree, Policy> entry : bucketsToPolicies.entrySet()) {
      if (entry.getValue() == null) {
        // no policy found for the bucket
        ctx.reportIssue(entry.getKey().labels().get(0), BucketsInsecureHttpPolicyValidator.MESSAGE);
      } else {
        checkBucketPolicy(ctx, entry.getKey(), entry.getValue());
      }
    }
  }

  private static void checkBucketPolicy(CheckContext ctx, BlockTree bucket, Policy policy) {
    if (VALIDATOR.isPolicySecure(policy)) {
      return;
    }

    List<SecondaryLocation> secondaryLocations = VALIDATOR.findInsecureFields(policy).entrySet().stream()
      .map(e -> new SecondaryLocation(e.getKey(), e.getValue()))
      .toList();

    ctx.reportIssue(bucket.labels().get(0), BucketsInsecureHttpPolicyValidator.MESSAGE, secondaryLocations);
  }

  private static Map<BlockTree, Policy> bucketsToPolicies(List<BlockTree> buckets, List<BlockTree> policies) {
    Map<Tree, BlockTree> bucketIdToPolicies = new HashMap<>();
    for (BlockTree policy : policies) {
      PropertyUtils.value(policy, "bucket").ifPresent(tree -> bucketIdToPolicies.put(tree, policy));
    }

    Map<BlockTree, Policy> result = new HashMap<>();
    for (BlockTree bucket : buckets) {
      // the bucket might directly contain the policy as an attribute
      List<Policy> nestedPolicies = PolicyUtils.getPolicies(bucket);
      if (!nestedPolicies.isEmpty()) {
        result.put(bucket, nestedPolicies.get(0));
        continue;
      }

      // if no nested policy was found, check if one of the collected aws_s3_bucket_policy resources are linked to the bucket
      Policy policy = bucketIdToPolicies.entrySet()
        .stream()
        .filter(e -> correspondsToBucket(e.getKey(), bucket))
        .map(Map.Entry::getValue)
        .map(tree -> PolicyUtils.getPolicies(tree).stream().findFirst())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElse(null);

      result.put(bucket, policy);
    }
    return result;
  }

  private static boolean correspondsToBucket(Tree key, BlockTree bucket) {
    if (key instanceof LiteralExprTree literalExpr) {
      return PropertyUtils.value(bucket, "bucket")
        .map(name -> TextUtils.isValue(name, literalExpr.value()).isTrue())
        .orElse(false);
    } else if (key instanceof AttributeAccessTree attributeAccess && attributeAccess.object() instanceof AttributeAccessTree accessTree && bucket.labels().size() >= 2) {
      return accessTree.attribute().value().equals(bucket.labels().get(1).value());
    }

    return false;
  }

  private static class BucketsAndPoliciesCollector extends TreeVisitor<TreeContext> {
    private final List<BlockTree> buckets = new ArrayList<>();
    private final List<BlockTree> policies = new ArrayList<>();

    public BucketsAndPoliciesCollector() {
      register(BlockTree.class, (ctx, tree) -> {
        if (isS3BucketResource(tree)) {
          buckets.add(tree);
        } else if (isResource(tree, "aws_s3_bucket_policy")) {
          policies.add(tree);
        }
      });
    }
  }

  /**
   * Terraform-specific resource value handling: a {@code TupleTree} of resources is insecure when every
   * element is insecure; a {@code TemplateExpressionTree} is secure when its last part ends with {@code "*"};
   * a plain {@code TextTree} is secure when its value ends with {@code "*"}. Other shapes are conservatively insecure.
   *
   * <p>{@code TupleTree} satisfies {@code Iterable<?>}, so the iterable branch handles both native
   * Terraform tuples and the JSON-derived tuples produced by {@link org.sonar.iac.terraform.api.tree.HeredocLiteralTree}.
   */
  private static boolean isInsecureResource(Tree resource) {
    if (resource instanceof Iterable<?> iterable) {
      // empty list does not cover any resource → insecure; non-empty list is insecure only if every element is insecure
      return StreamSupport.stream(iterable.spliterator(), false)
        .allMatch(element -> element instanceof Tree tree && isInsecureResource(tree));
    }
    if (resource instanceof TextTree || resource instanceof TemplateExpressionTree) {
      return !isResourceIdentifierSecure(resource);
    }
    return true;
  }

  private static boolean isResourceIdentifierSecure(Tree resourceIdentifier) {
    if (resourceIdentifier instanceof TemplateExpressionTree templateExpression) {
      List<ExpressionTree> parts = templateExpression.parts();
      return !parts.isEmpty() && isResourceIdentifierSecure(parts.get(parts.size() - 1));
    }
    if (resourceIdentifier instanceof TextTree textTree) {
      return textTree.value().endsWith("*");
    }
    return true;
  }

}
