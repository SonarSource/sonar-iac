/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.checks.AbstractResourceCheck.Resource;
import org.sonar.iac.cloudformation.checks.utils.XPathUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.AttributeUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6249")
public class BucketsInsecureHttpCheck implements IacCheck {
  private static final String MESSAGE = "Make sure authorizing HTTP requests is safe here.";
  private static final String MESSAGE_SECONDARY_EFFECT = "Non-conforming requests should be denied.";
  private static final String MESSAGE_SECONDARY_CONDITION = "HTTPS requests are denied.";
  private static final String MESSAGE_SECONDARY_PRINCIPAL = "All principals should be restricted.";
  private static final String MESSAGE_SECONDARY_ACTION = "All S3 actions should be restricted.";
  private static final String MESSAGE_SECONDARY_RESOURCE = "All resources should be restricted.";

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
    for (Map.Entry<Resource, Resource> bucketToPolicy : bucketsToPolicies.entrySet()) {
      if (bucketToPolicy.getValue() == null) {
        ctx.reportIssue(bucketToPolicy.getKey().type(), MESSAGE);
      } else {
        checkBucketPolicy(ctx, bucketToPolicy.getKey(), bucketToPolicy.getValue());
      }
    }
  }

  private static void checkBucketPolicy(CheckContext ctx, Resource bucket, Resource policy) {
    Map<CloudformationTree, String> insecureValues = PolicyValidator.getInsecureValues(policy);
    if (!insecureValues.isEmpty()) {
      List<SecondaryLocation> secondaryLocations = insecureValues.entrySet().stream()
        .map(e -> new SecondaryLocation(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
      ctx.reportIssue(bucket.type(), MESSAGE, secondaryLocations);
    }
  }

  private static Map<Resource, Resource> bucketsToPolicies(List<Resource> buckets, List<Resource> policies) {
    Map<CloudformationTree, Resource> bucketIdToPolicies = new HashMap<>();
    for (Resource policy : policies) {
      AttributeUtils.<CloudformationTree>value(policy.properties(), "Bucket").ifPresent(b -> bucketIdToPolicies.put(b, policy));
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

  private static boolean correspondsToBucket(@Nullable CloudformationTree policyBucketId, Resource bucket) {
    if (policyBucketId instanceof MappingTree) {
      // In JSON format to reference a bucket, an object having a Ref field has to be provided
      return AttributeUtils.value(policyBucketId, "Ref")
        .filter(ScalarTree.class::isInstance)
        .filter(ref -> TextUtils.isValue(bucket.name(), ((ScalarTree) ref).value()).isTrue())
        .isPresent();
    }

    if (!(policyBucketId instanceof ScalarTree)) {
      return false;
    }

    String policyBucketIdValue = ((ScalarTree) policyBucketId).value();
    if ("!Ref".equals(policyBucketId.tag())) {
      return TextUtils.isValue(bucket.name(), policyBucketIdValue).isTrue();
    }

    Optional<CloudformationTree> bucketName = AttributeUtils.value(bucket.properties(), "BucketName");
    return bucketName.isPresent() && TextUtils.isValue(bucketName.get(), policyBucketIdValue).isTrue();
  }

  private static class PolicyValidator {

    private static Map<CloudformationTree, String> getInsecureValues(Resource policy) {
      HashMap<CloudformationTree, String> result = new HashMap<>();

      Optional<CloudformationTree> statement = XPathUtils.getSingleTree(policy.properties(), "/PolicyDocument/Statement[]");
      if (statement.isPresent()) {
        XPathUtils.getSingleTree(statement.get(), "/Effect")
          .filter(PolicyValidator::isInsecureEffect).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_EFFECT));
        XPathUtils.getSingleTree(statement.get(), "/Condition/Bool/aws:SecureTransport")
          .filter(PolicyValidator::isInsecureCondition).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_CONDITION));
        XPathUtils.getSingleTree(statement.get(), "/Action")
          .filter(PolicyValidator::isInsecureAction).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_ACTION));
        XPathUtils.getSingleTree(statement.get(), "/Principal")
          .filter(PolicyValidator::isInsecurePrincipal).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_PRINCIPAL));
        XPathUtils.getSingleTree(statement.get(), "/Resource")
          .filter(PolicyValidator::isInsecureResource).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_RESOURCE));
      }

      return result;
    }

    private static boolean isInsecureResource(CloudformationTree resource) {
      return !(resource instanceof ScalarTree && ((ScalarTree) resource).value().endsWith("*"));
    }

    private static boolean isInsecurePrincipal(CloudformationTree principal) {
      CloudformationTree valueToCheck = principal;
      if (principal instanceof MappingTree) {
        valueToCheck = AttributeUtils.valueOrNull(principal, "AWS");
      }
      return !TextUtils.isValue(valueToCheck, "*").isTrue();
    }

    private static boolean isInsecureAction(CloudformationTree action) {
      return !(TextUtils.isValue(action, "*").isTrue() || TextUtils.isValue(action, "s3:*").isTrue());
    }

    private static boolean isInsecureCondition(CloudformationTree condition) {
      return TextUtils.isValue(condition, "true").isTrue();
    }

    private static boolean isInsecureEffect(CloudformationTree effect) {
      return TextUtils.isValue(effect, "Allow").isTrue();
    }
  }
}
