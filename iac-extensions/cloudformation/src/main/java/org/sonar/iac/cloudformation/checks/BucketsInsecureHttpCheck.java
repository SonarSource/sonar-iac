package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.checks.AbstractResourceCheck.Resource;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.cloudformation.checks.utils.ScalarTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

@Rule(key = "S6249")
public class BucketsInsecureHttpCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      List<Resource> buckets = new ArrayList<>();
      List<Resource> policies = new ArrayList<>();
      AbstractResourceCheck.getFileResources(tree).forEach(r -> {
        if (AbstractResourceCheck.isS3Bucket(r)) {
          buckets.add(r);
        } else if (r.isType("AWS::S3::BucketPolicy")) {
          policies.add(r);
        }
      });
      checkBucketsAndPolicies(ctx, bucketsToPolicies(buckets, policies));
    });
  }

  private static void checkBucketsAndPolicies(CheckContext ctx, Map<Resource, Resource> bucketsToPolicies) {
    // TODO: Implement
  }

  private static Map<Resource, Resource> bucketsToPolicies(List<Resource> buckets, List<Resource> policies) {
    Map<CloudformationTree, Resource> bucketIdToPolicies = new HashMap<>();
    for (Resource policy : policies) {
      MappingTreeUtils.getValue(policy.properties(), "Bucket").ifPresent(b -> bucketIdToPolicies.put(b, policy));
    }

    Map<Resource, Resource> result = new HashMap<>();
    for (Resource bucket : buckets) {
      Resource policy = bucketIdToPolicies.entrySet().stream()
        .filter(e -> correspondsToBucket(e.getKey(), bucket))
        .map(Map.Entry::getValue)
        .findFirst().orElse(null);
      result.put(bucket, policy);
    }

    return result;
  }

  private static boolean correspondsToBucket(CloudformationTree policyBucketId, Resource bucket) {
    if (!(policyBucketId instanceof ScalarTree)) {
      return false;
    }

    String policyBucketIdValue = ((ScalarTree) policyBucketId).value();
    if ("!Ref".equals(policyBucketId.tag())) {
      return ScalarTreeUtils.isValue(bucket.name(), policyBucketIdValue);
    }

    Optional<CloudformationTree> bucketName = MappingTreeUtils.getValue(bucket.properties(), "BucketName");
    return bucketName.isPresent() && ScalarTreeUtils.isValue(bucketName.get(), policyBucketIdValue);
  }
}
