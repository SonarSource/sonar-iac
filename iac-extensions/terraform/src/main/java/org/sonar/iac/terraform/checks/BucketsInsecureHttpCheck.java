/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TemplateExpressionTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck.Policy;
import org.sonar.iac.terraform.checks.utils.ObjectUtils;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isS3BucketResource;

@Rule(key = "S6249")
public class BucketsInsecureHttpCheck implements IacCheck {
  private static final String MESSAGE = "Make sure authorizing HTTP requests is safe here.";
  private static final String MESSAGE_SECONDARY_CONDITION = "HTTPS requests are denied.";
  private static final String MESSAGE_SECONDARY_EFFECT = "Non-conforming requests should be denied.";
  private static final String MESSAGE_SECONDARY_ACTION = "All S3 actions should be restricted.";
  private static final String MESSAGE_SECONDARY_PRINCIPAL = "All principals should be restricted.";
  private static final String MESSAGE_SECONDARY_RESOURCE = "All resources should be restricted.";

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
        ctx.reportIssue(entry.getKey().labels().get(0), MESSAGE);
      } else {
        checkBucketPolicy(ctx, entry.getKey(), entry.getValue());
      }
    }
  }

  private static void checkBucketPolicy(CheckContext ctx, BlockTree bucket, Policy policy) {
    Map<ExpressionTree, String> insecureValues = PolicyValidator.getInsecureValues(policy);

    if (insecureValues.isEmpty()) {
      return;
    }

    List<SecondaryLocation> secondaryLocations = insecureValues.entrySet().stream()
      .filter(e -> e.getKey() != null)
      .map(e -> new SecondaryLocation(e.getKey(), e.getValue()))
      .collect(Collectors.toList());

    ctx.reportIssue(bucket.labels().get(0), MESSAGE, secondaryLocations);
  }

  private static Map<BlockTree, Policy> bucketsToPolicies(List<BlockTree> buckets, List<BlockTree> policies) {
    Map<Tree, BlockTree> bucketIdToPolicies = new HashMap<>();
    for (BlockTree policy : policies) {
      StatementUtils.getAttributeValue(policy, "bucket").ifPresent(tree -> bucketIdToPolicies.put(tree, policy));
    }

    Map<BlockTree, Policy> result = new HashMap<>();
    for (BlockTree bucket : buckets) {
      // the bucket might directly contain the policy as an attribute
      Optional<ExpressionTree> nestedPolicy = StatementUtils.getAttributeValue(bucket, "policy");
      if (nestedPolicy.isPresent()) {
        result.put(bucket, Policy.from(nestedPolicy.get()));
        continue;
      }

      // if no nested policy was found, check if one of the collected aws_s3_bucket_policy resources are linked to the bucket
      Optional<ExpressionTree> policyTree = bucketIdToPolicies.entrySet().stream()
        .filter(e -> correspondsToBucket(e.getKey(), bucket))
        .map(Map.Entry::getValue)
        .map(p -> StatementUtils.getAttributeValue(p, "policy"))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();

      if (policyTree.isPresent()) {
        result.put(bucket, Policy.from(policyTree.get()));
      } else {
        result.put(bucket, null);
      }
    }
    return result;
  }

  private static boolean correspondsToBucket(Tree key, BlockTree bucket) {
    if (key instanceof LiteralExprTree) {
      Optional<ExpressionTree> name = StatementUtils.getAttributeValue(bucket, "bucket");
      if (name.isPresent() && name.get() instanceof LiteralExprTree) {
        return ((LiteralExprTree) key).token().value().equals(((LiteralExprTree) name.get()).token().value());
      }
    } else if (key instanceof AttributeAccessTree && ((AttributeAccessTree) key).object() instanceof AttributeAccessTree && bucket.labels().size() >= 2) {
      AttributeAccessTree object = (AttributeAccessTree) ((AttributeAccessTree) key).object();
      return object.attribute().value().equals(bucket.labels().get(1).value().replaceAll("(^\")|(\"$)", ""));
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
        } else if (isResource(tree, "\"aws_s3_bucket_policy\"")) {
          policies.add(tree);
        }
      });
    }
  }

  private static class PolicyValidator {

    public static Map<ExpressionTree, String> getInsecureValues(Policy policy) {
      Map<ExpressionTree, String> result = new HashMap<>();

      policy.effect().filter(PolicyValidator::isInsecureEffect)
        .ifPresent(effect -> result.put(effect, MESSAGE_SECONDARY_EFFECT));

      policy.condition().filter(PolicyValidator::isInsecureCondition)
        .ifPresent(condition -> result.put(condition, MESSAGE_SECONDARY_CONDITION));

      policy.action().filter(PolicyValidator::isInsecureAction)
        .ifPresent(action -> result.put(action, MESSAGE_SECONDARY_ACTION));

      policy.principal().filter(PolicyValidator::isInsecurePrincipal)
        .ifPresent(principal -> result.put(principal, MESSAGE_SECONDARY_PRINCIPAL));

      policy.resource().filter(PolicyValidator::isInsecureResource)
        .ifPresent(resource -> result.put(resource, MESSAGE_SECONDARY_RESOURCE));

      return result;
    }

    private static boolean isInsecureResource(ExpressionTree resource) {
      List<Tree> resourceIdentifiers = new ArrayList<>();

      if (resource instanceof LiteralExprTree || resource instanceof TemplateExpressionTree) {
        resourceIdentifiers.add(resource);
      } else if (resource instanceof TupleTree) {
        resourceIdentifiers.addAll(((TupleTree) resource).elements().trees());
      }

      for (Tree resourceIdentifier : resourceIdentifiers) {
        if (isResourceIdentifierSecure(resourceIdentifier)) {
          return false;
        }
      }

      return !resourceIdentifiers.isEmpty();
    }

    private static boolean isResourceIdentifierSecure(Tree resourceIdentifier) {
      if (resourceIdentifier instanceof LiteralExprTree) {
        return ((LiteralExprTree) resourceIdentifier).value().endsWith("*");
      } else if (resourceIdentifier instanceof TemplateExpressionTree) {
        List<ExpressionTree> parts = ((TemplateExpressionTree) resourceIdentifier).parts();
        return !parts.isEmpty() && isResourceIdentifierSecure(parts.get(parts.size() - 1));
      }
      return true;
    }

    private static boolean isInsecurePrincipal(ExpressionTree principal) {
      Optional<ExpressionTree> aws = ObjectUtils.getElementValue(principal, "AWS");
      return aws.isPresent() && (aws.get() instanceof TupleTree || (aws.get() instanceof LiteralExprTree && !"*".equals(((LiteralExprTree) aws.get()).value())));
    }

    private static boolean isInsecureAction(ExpressionTree action) {
      return action instanceof LiteralExprTree && !("*".equals(((LiteralExprTree) action).value()) || "s3:*".equals(((LiteralExprTree) action).value()));
    }

    private static boolean isInsecureEffect(ExpressionTree effect) {
      return effect instanceof LiteralExprTree && !"Deny".equals(((LiteralExprTree) effect).value());
    }

    private static boolean isInsecureCondition(ExpressionTree condition) {
      Optional<ExpressionTree> bool = ObjectUtils.getElementValue(condition, "Bool");
      if (!(bool.isPresent() && bool.get() instanceof ObjectTree)) {
        return false;
      }

      Optional<ExpressionTree> secureTransport = ObjectUtils.getElementValue((ObjectTree) bool.get(), "aws:SecureTransport");
      return secureTransport.isPresent() && secureTransport.get() instanceof LiteralExprTree &&
        !"false".equals(((LiteralExprTree) secureTransport.get()).value());
    }
  }

}
