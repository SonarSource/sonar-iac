/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isS3BucketResource;

@Rule(key = "S6281")
public class BucketsPublicAclOrPolicyCheck implements IacCheck {

  private static final String MESSAGE = "Make sure allowing public policy/acl access is safe here.";
  private static final String SECONDARY_MSG_PROPERTY = "Set this property to true";
  private static final String SECONDARY_MSG_BUCKET = "Related bucket";
  private static final String PAB = "aws_s3_bucket_public_access_block";

  private static final Set<String> PAB_STATEMENTS = new HashSet<>(Arrays.asList(
    "block_public_policy",
    "block_public_acls",
    "ignore_public_acls",
    "restrict_public_buckets"
  ));

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> BucketAndResourceCollector.getResolvedS3Buckets(tree)
      .forEach(bucket -> checkS3Bucket(ctx, bucket)));
  }

  private static void checkS3Bucket(CheckContext ctx, S3Bucket bucket) {
    Optional<BlockTree> publicAccessBlock = bucket.resource(PAB);
    if (publicAccessBlock.isPresent())  {
      LabelTree publicAccessBlockType = publicAccessBlock.get().labels().get(0);
      List<SecondaryLocation> secLoc = checkPublicAccessBlock(publicAccessBlock.get());
      if (!secLoc.isEmpty() || hasMissingStatement(publicAccessBlock.get())) {
        secLoc.add(new SecondaryLocation(bucket.label(), SECONDARY_MSG_BUCKET));
        ctx.reportIssue(publicAccessBlockType, MESSAGE, secLoc);
      }
    } else {
      ctx.reportIssue(bucket.label(), MESSAGE);
    }
  }

  private static List<SecondaryLocation> checkPublicAccessBlock(BlockTree publicAccessBlock) {
    return PAB_STATEMENTS.stream()
      .map(e -> StatementUtils.getAttributeValue(publicAccessBlock, e))
      .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
      .filter(TextUtils::isValueFalse)
      .map(value -> new SecondaryLocation(value, SECONDARY_MSG_PROPERTY))
      .collect(Collectors.toList());
  }

  private static boolean hasMissingStatement(BlockTree publicAccessBlock) {
    return PAB_STATEMENTS.stream().anyMatch(e -> !StatementUtils.hasStatement(publicAccessBlock, e));
  }

  private static class S3Bucket {
    private final Map<String, BlockTree> resources = new HashMap<>();
    private final LabelTree label;
    private final String resourceName;
    private final String bucketName;

    private S3Bucket(BlockTree bucket) {
      this.label = bucket.labels().get(0);
      this.resourceName = bucket.labels().size() >= 2 ? bucket.labels().get(1).value() : null;
      this.bucketName = StatementUtils.getAttributeValue(bucket, "bucket")
        .filter(LiteralExprTree.class::isInstance).map(e -> ((LiteralExprTree) e).value()).orElse(null);
    }

    private void assignResource(BlockTree resource) {
      resources.put(resource.labels().get(0).value(), resource);
    }

    public Optional<BlockTree> resource(String resourceName) {
      return Optional.ofNullable(resources.getOrDefault(resourceName, null));
    }

    public LabelTree label() {
      return label;
    }
  }

  private static class BucketAndResourceCollector extends TreeVisitor<TreeContext> {
    private final List<S3Bucket> buckets = new ArrayList<>();
    private final List<BlockTree> resources = new ArrayList<>();

    public BucketAndResourceCollector() {
      register(BlockTree.class, (ctx, tree) -> {
        if (isS3BucketResource(tree)) {
          S3Bucket bucket = new S3Bucket(tree);
          buckets.add(bucket);
        } else if (isResource(tree)) {
          resources.add(tree);
        }
      });
    }

    public static List<S3Bucket> getResolvedS3Buckets(FileTree tree) {
      BucketAndResourceCollector collector = new BucketAndResourceCollector();
      collector.scan(new TreeContext(), tree);
      return collector.getAssignedBuckets();
    }

    @Override
    protected void after(TreeContext ctx, Tree root) {
      resources.stream().filter(resource -> !resource.labels().isEmpty())
        .forEach(resource -> StatementUtils.getAttributeValue(resource, "bucket").ifPresent(identifier -> {
          if (identifier.is(TerraformTree.Kind.STRING_LITERAL)) {
            assignByBucketName((LiteralExprTree) identifier, resource);
          } else if (identifier.is(TerraformTree.Kind.ATTRIBUTE_ACCESS)) {
            assignByResourceName((AttributeAccessTree) identifier, resource);
          }
        }));
    }

    private void assignByResourceName(AttributeAccessTree identifier, BlockTree resource) {
      if (identifier.object().is(TerraformTree.Kind.ATTRIBUTE_ACCESS)) {
        String name = ((AttributeAccessTree) identifier.object()).attribute().value();
        buckets.stream().filter(bucket -> name.equals(bucket.resourceName)).forEach(bucket -> bucket.assignResource(resource));
      }
    }

    private void assignByBucketName(LiteralExprTree bucketName, BlockTree resource) {
      buckets.stream().filter(bucket -> bucketName.value().equals(bucket.bucketName)).forEach(bucket -> bucket.assignResource(resource));
    }

    private List<S3Bucket> getAssignedBuckets() {
      return buckets;
    }
  }

}
