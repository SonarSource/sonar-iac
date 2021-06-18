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
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;

@Rule(key = "S6249")
public class BucketsInsecureHttpCheck implements IacCheck {
  private static final String MESSAGE = "Make sure authorizing HTTP requests is safe here.";

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      BucketsAndPoliciesCollector collector = new BucketsAndPoliciesCollector();
      // TODO: 21.06.21 not pass null here. Find another solution
      collector.scan(new InputFileContext(null, null), tree);
      checkBucketsAndPolicies(ctx, bucketsToPolicies(collector.buckets, collector.policies));
    });
  }

  private static void checkBucketsAndPolicies(CheckContext ctx, Map<BlockTree, BlockTree> bucketsToPolicies) {
    for (Map.Entry<BlockTree, BlockTree> entry : bucketsToPolicies.entrySet()) {
      if (entry.getValue() == null) {
        ctx.reportIssue(entry.getKey().labels().get(0), MESSAGE);
      }
    }
  }

  private static Map<BlockTree, BlockTree> bucketsToPolicies(List<BlockTree> buckets, List<BlockTree> policies) {
    Map<Tree, BlockTree> bucketIdToPolicies = new HashMap<>();
    for (BlockTree policy : policies) {
      getAttributeValue(policy, "bucket").ifPresent(tree -> bucketIdToPolicies.put(tree, policy));
    }

    Map<BlockTree, BlockTree> result = new HashMap<>();
    for (BlockTree bucket : buckets) {
      Optional<BlockTree> policy = bucketIdToPolicies.entrySet().stream()
        .filter(e -> correspondsToBucket(e.getKey(), bucket))
        .map(Map.Entry::getValue)
        .findFirst();
      result.put(bucket, policy.orElse(null));
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

  private static class BucketsAndPoliciesCollector extends TreeVisitor<InputFileContext> {
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

    private static boolean isPolicy(BlockTree tree) {
      return !tree.labels().isEmpty() && "\"aws_s3_bucket_policy\"".equals(tree.labels().get(0).value());
    }

    private static boolean isS3Bucket(BlockTree tree) {
      return !tree.labels().isEmpty() && "\"aws_s3_bucket\"".equals(tree.labels().get(0).value());
    }
  }
}
