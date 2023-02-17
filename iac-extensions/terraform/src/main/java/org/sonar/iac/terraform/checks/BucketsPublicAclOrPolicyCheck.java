/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isS3BucketResource;

@Rule(key = "S6281")
public class BucketsPublicAclOrPolicyCheck implements IacCheck {

  private static final String MESSAGE = "Make sure allowing public ACL/policies to be set is safe here.";
  private static final String OMITTING_MESSAGE = "No Public Access Block configuration prevents public ACL/policies to be set on this S3 bucket. Make sure it is safe here.";
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
    init.register(FileTree.class, (ctx, tree) -> {
      BucketAndResourceCollector collector = BucketAndResourceCollector.collect(tree);
      collector.getAssignedBuckets().forEach(bucket -> checkS3Bucket(ctx, bucket, collector.getPublicAccessBlocks()));
      collector.getPublicAccessBlocks().forEach(resource -> checkPublicAccessBlocks(ctx, resource, null));
    });
  }

  private static void checkS3Bucket(CheckContext ctx, S3Bucket bucket, Set<BlockTree> publicAccessBlocks) {
    Optional<BlockTree> publicAccessBlock = bucket.resource(PAB);
    if (publicAccessBlock.isPresent())  {
      BlockTree pab = publicAccessBlock.get();
      publicAccessBlocks.remove(pab);
      checkPublicAccessBlocks(ctx, pab, bucket);
    } else {
      ctx.reportIssue(bucket.label(), OMITTING_MESSAGE);
    }
  }

  private static void checkPublicAccessBlocks(CheckContext ctx, BlockTree pab, @Nullable S3Bucket s3Bucket) {
    List<SecondaryLocation> secLoc = checkWrongConfiguration(pab);
    if (!secLoc.isEmpty() || hasMissingStatement(pab)) {
      if (s3Bucket != null) {
        secLoc.add(new SecondaryLocation(s3Bucket.label(), SECONDARY_MSG_BUCKET));
      }
      ctx.reportIssue(pab.labels().get(0), MESSAGE, secLoc);
    }
  }

  private static List<SecondaryLocation> checkWrongConfiguration(BlockTree publicAccessBlock) {
    return PAB_STATEMENTS.stream()
      .map(e -> PropertyUtils.value(publicAccessBlock, e))
      .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
      .filter(TextUtils::isValueFalse)
      .map(value -> new SecondaryLocation(value, SECONDARY_MSG_PROPERTY))
      .collect(Collectors.toList());
  }

  private static boolean hasMissingStatement(BlockTree publicAccessBlock) {
    return PAB_STATEMENTS.stream().anyMatch(e -> PropertyUtils.isMissing(publicAccessBlock, e));
  }

  private static class S3Bucket {
    private final Map<String, BlockTree> resources = new HashMap<>();
    private final LabelTree label;
    private final String resourceName;
    private final String bucketName;

    private S3Bucket(BlockTree bucket) {
      this.label = bucket.labels().get(0);
      this.resourceName = bucket.labels().size() >= 2 ? bucket.labels().get(1).value() : null;
      this.bucketName = PropertyUtils.value(bucket, "bucket")
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
    private final Set<BlockTree> publicAccessBlocks = new LinkedHashSet<>();
    private final List<BlockTree> resources = new ArrayList<>();

    public BucketAndResourceCollector() {
      register(BlockTree.class, (ctx, tree) -> {
        if (isS3BucketResource(tree)) {
          S3Bucket bucket = new S3Bucket(tree);
          buckets.add(bucket);
        } else if (isResource(tree)) {
          if (isResource(tree, PAB)) {
            publicAccessBlocks.add(tree);
          }
          resources.add(tree);
        }
      });
    }

    public static BucketAndResourceCollector collect(FileTree tree) {
      BucketAndResourceCollector collector = new BucketAndResourceCollector();
      collector.scan(new TreeContext(), tree);
      return collector;
    }

    @Override
    protected void after(TreeContext ctx, Tree root) {
      resources.stream().filter(resource -> !resource.labels().isEmpty())
        .forEach(resource -> PropertyUtils.value(resource, "bucket", TerraformTree.class).ifPresent(identifier -> {
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

    private Set<BlockTree> getPublicAccessBlocks() {
      return publicAccessBlocks;
    }
  }

}
