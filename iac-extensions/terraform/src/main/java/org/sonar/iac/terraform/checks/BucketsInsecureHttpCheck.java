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
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TemplateExpressionTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;

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
    Map<Tree, String> insecureValues = policy.getInsecureValues();

    if (insecureValues.isEmpty()) {
      return;
    }

    List<SecondaryLocation> secondaryLocations = new ArrayList<>();
    insecureValues.entrySet().stream()
      .filter(e -> e.getKey() != null)
      .forEach(e -> secondaryLocations.add(new SecondaryLocation(e.getKey(), e.getValue())));

    ctx.reportIssue(bucket.labels().get(0), MESSAGE, secondaryLocations);
  }

  private static Map<BlockTree, Policy> bucketsToPolicies(List<BlockTree> buckets, List<BlockTree> policies) {
    Map<Tree, BlockTree> bucketIdToPolicies = new HashMap<>();
    for (BlockTree policy : policies) {
      getAttributeValue(policy, "bucket").ifPresent(tree -> bucketIdToPolicies.put(tree, policy));
    }

    Map<BlockTree, Policy> result = new HashMap<>();
    for (BlockTree bucket : buckets) {
      // the bucket might directly contain the policy as an attribute
      Optional<Tree> nestedPolicy = getAttributeValue(bucket, "policy");
      if (nestedPolicy.isPresent()) {
        result.put(bucket, Policy.from(nestedPolicy.get()));
        continue;
      }

      // if no nested policy was found, check if one of the collected aws_s3_bucket_policy resources are linked to the bucket
      Optional<Tree> policyTree = bucketIdToPolicies.entrySet().stream()
        .filter(e -> correspondsToBucket(e.getKey(), bucket))
        .map(Map.Entry::getValue)
        .map(p -> getAttributeValue(p, "policy"))
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
      Optional<Tree> name = getAttributeValue(bucket, "bucket");
      if (name.isPresent() && name.get() instanceof LiteralExprTree) {
        return ((LiteralExprTree) key).token().value().equals(((LiteralExprTree) name.get()).token().value());
      }
    } else if (key instanceof AttributeAccessTree && ((AttributeAccessTree) key).object() instanceof AttributeAccessTree && bucket.labels().size() >= 2) {
      AttributeAccessTree object = (AttributeAccessTree) ((AttributeAccessTree) key).object();
      return object.attribute().value().equals(bucket.labels().get(1).value().replaceAll("(^\")|(\"$)", ""));
    }

    return false;
  }

  private static Optional<Tree> getAttributeValue(BlockTree block, String name) {
    Optional<BodyTree> body = block.body();
    if (body.isPresent()) {
      for (Tree statement : body.get().statements()) {
        if (statement instanceof AttributeTree && name.equals(((AttributeTree) statement).name().value())) {
          return Optional.of(((AttributeTree)statement).value());
        }
      }
    }

    return Optional.empty();
  }

  private static class BucketsAndPoliciesCollector extends TreeVisitor<TreeContext> {
    private final List<BlockTree> buckets = new ArrayList<>();
    private final List<BlockTree> policies = new ArrayList<>();

    public BucketsAndPoliciesCollector() {
      register(BlockTree.class, (ctx, tree) -> {
        if (isS3Bucket(tree)) {
          buckets.add(tree);
        } else if (isPolicy(tree)) {
          policies.add(tree);
        }
      });
    }

    protected static boolean isPolicy(BlockTree tree) {
      return !tree.labels().isEmpty() && "\"aws_s3_bucket_policy\"".equals(tree.labels().get(0).value());
    }

    private static boolean isS3Bucket(BlockTree tree) {
      return !tree.labels().isEmpty() && "\"aws_s3_bucket\"".equals(tree.labels().get(0).value());
    }
  }

  private static class Policy {
    private final Tree effect;
    private final Tree principal;
    private final Tree action;
    private final Tree resource;
    private final Tree condition;

    private Policy(@Nullable Tree effect, @Nullable Tree principal, @Nullable Tree action, @Nullable Tree resource, @Nullable Tree condition) {
      this.effect = effect;
      this.principal = principal;
      this.action = action;
      this.resource = resource;
      this.condition = condition;
    }

    private Policy() {
      this(null, null, null, null, null);
    }

    private static Policy from(Tree policy) {
      if (!(policy instanceof FunctionCallTree) || ((FunctionCallTree) policy).arguments().trees().isEmpty()) {
        return new Policy();
      }

      ExpressionTree policyArgument = ((FunctionCallTree) policy).arguments().trees().get(0);
      if (!(policyArgument instanceof ObjectTree)) {
        return new Policy();
      }

      Optional<ExpressionTree> statementField = getFieldValue((ObjectTree)policyArgument, "Statement");
      if (!statementField.isPresent() || !(statementField.get() instanceof TupleTree) ||
        ((TupleTree) statementField.get()).elements().trees().isEmpty() ||
        !(((TupleTree) statementField.get()).elements().trees().get(0) instanceof ObjectTree)) {
        return new Policy();
      }

      ObjectTree policyStatement = (ObjectTree) ((TupleTree) statementField.get()).elements().trees().get(0);

      return new Policy(getFieldValue(policyStatement, "Effect").orElse(null),
        getFieldValue(policyStatement, "Principal").orElse(null),
        getFieldValue(policyStatement, "Action").orElse(null),
        getFieldValue(policyStatement, "Resource").orElse(null),
        getFieldValue(policyStatement, "Condition").orElse(null));
    }

    private static Optional<ExpressionTree> getFieldValue(ObjectTree policyArgument, String name) {
      for (ObjectElementTree tree : policyArgument.elements().trees()) {
        if ((tree.name() instanceof VariableExprTree && name.equals(((VariableExprTree) tree.name()).name())) ||
          (tree.name() instanceof LiteralExprTree && name.equals(((LiteralExprTree) tree.name()).value()))) {
          return Optional.of(tree.value());
        }
      }
      return Optional.empty();
    }

    private Map<Tree, String> getInsecureValues() {
      Map<Tree, String> result = new HashMap<>();
      if (isInsecureEffect()) {
        result.put(effect, MESSAGE_SECONDARY_EFFECT);
      }
      if (isInsecureCondition()) {
        result.put(condition, MESSAGE_SECONDARY_CONDITION);
      }
      if (isInsecureAction()) {
        result.put(action, MESSAGE_SECONDARY_ACTION);
      }
      if (isInsecurePrincipal()) {
        result.put(principal, MESSAGE_SECONDARY_PRINCIPAL);
      }
      if (isInsecureResource()) {
        result.put(resource, MESSAGE_SECONDARY_RESOURCE);
      }

      return result;
    }

    private boolean isInsecureResource() {
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

    private boolean isInsecurePrincipal() {
      if (!(principal instanceof ObjectTree)) {
        return false;
      }

      Optional<ExpressionTree> aws = getFieldValue((ObjectTree) principal, "AWS");
      return aws.isPresent() && (aws.get() instanceof TupleTree || (aws.get() instanceof LiteralExprTree && !"*".equals(((LiteralExprTree) aws.get()).value())));
    }

    private boolean isInsecureAction() {
      return action instanceof LiteralExprTree && !("*".equals(((LiteralExprTree) action).value()) || "s3:*".equals(((LiteralExprTree) action).value()));
    }

    private boolean isInsecureEffect() {
      return effect instanceof LiteralExprTree && !"Deny".equals(((LiteralExprTree) effect).value());
    }

    private boolean isInsecureCondition() {
      if (!(condition instanceof ObjectTree)) {
        return false;
      }

      Optional<ExpressionTree> bool = getFieldValue((ObjectTree) condition, "Bool");
      if (!bool.isPresent() || !(bool.get() instanceof ObjectTree)) {
        return false;
      }

      Optional<ExpressionTree> secureTransport = getFieldValue((ObjectTree) bool.get(), "aws:SecureTransport");
      return secureTransport.isPresent() && secureTransport.get() instanceof LiteralExprTree &&
        !"false".equals(((LiteralExprTree) secureTransport.get()).value());
    }
  }

}
