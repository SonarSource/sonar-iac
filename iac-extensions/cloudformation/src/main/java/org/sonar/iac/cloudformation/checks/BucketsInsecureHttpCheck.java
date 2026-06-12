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
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.checks.AbstractResourceCheck.Resource;
import org.sonar.iac.cloudformation.tree.FunctionCallTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.policy.BucketsInsecureHttpPolicyValidator;
import org.sonar.iac.common.checks.policy.Policy;
import org.sonar.iac.common.checks.policy.Policy.Statement;
import org.sonar.iac.common.yaml.XPathUtils;
import org.sonar.iac.common.yaml.YamlTreeUtils;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6249")
public class BucketsInsecureHttpCheck implements IacCheck {

  private static final BucketsInsecureHttpPolicyValidator VALIDATOR = new BucketsInsecureHttpPolicyValidator(BucketsInsecureHttpCheck::isInsecureResource);

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
        ctx.reportIssue(bucketToPolicy.getKey().type(), BucketsInsecureHttpPolicyValidator.MESSAGE);
      } else {
        checkBucketPolicy(ctx, bucketToPolicy.getKey(), bucketToPolicy.getValue());
      }
    }
  }

  private static void checkBucketPolicy(CheckContext ctx, Resource bucket, Resource policyResource) {
    Policy policy = buildPolicy(policyResource);
    if (VALIDATOR.isPolicySecure(policy)) {
      return;
    }
    List<SecondaryLocation> secondaryLocations = VALIDATOR.findInsecureFields(policy).entrySet().stream()
      .map(e -> new SecondaryLocation(e.getKey(), e.getValue()))
      .toList();
    ctx.reportIssue(bucket.type(), BucketsInsecureHttpPolicyValidator.MESSAGE, secondaryLocations);
  }

  /**
   * Build a common {@link Policy} from a CloudFormation {@code AWS::S3::BucketPolicy} resource by extracting
   * the {@code /PolicyDocument/Statement[]} subtree. Returns an empty {@link Policy} (no statements) when the
   * resource has no parseable properties — the common validator then treats that as compliant.
   */
  private static Policy buildPolicy(Resource policyResource) {
    YamlTree properties = policyResource.properties();
    if (properties == null) {
      return new Policy(null, null, Collections.emptyList());
    }
    List<Statement> statements = XPathUtils.getTrees(properties, "/PolicyDocument/Statement[]").stream().map(Statement::new).toList();
    return new Policy(
      PropertyUtils.valueOrNull(properties, "Version"),
      PropertyUtils.valueOrNull(properties, "Id"),
      statements);
  }

  private static Map<Resource, Resource> bucketsToPolicies(List<Resource> buckets, List<Resource> policies) {
    Map<YamlTree, Resource> bucketIdToPolicies = new HashMap<>();
    for (Resource policy : policies) {
      PropertyUtils.value(policy.properties(), "Bucket", YamlTree.class)
        .ifPresent(b -> bucketIdToPolicies.put(b, policy));
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

  private static boolean correspondsToBucket(@Nullable YamlTree policyBucketId, Resource bucket) {
    if (policyBucketId instanceof FunctionCallTree functionCall && isReferringFunction(functionCall)) {
      return functionCall.arguments().stream()
        .map(TextUtils::getValue)
        .flatMap(Optional::stream)
        .anyMatch(argument -> TextUtils.isValue(bucket.name(), argument).isTrue());
    } else if (policyBucketId instanceof FunctionCallTree functionCall && isJoin(functionCall)) {
      return functionCall.arguments().stream()
        .map(YamlTreeUtils::getListValueElements)
        .flatMap(Collection::stream)
        // Remove the empty strings before checking for equality, as the bucket name can also be an empty string
        .filter(elem -> !"".equals(elem))
        .anyMatch(elementValue -> getNameOfBucket(bucket).equals(elementValue));
    } else if (policyBucketId instanceof ScalarTree scalar) {
      return PropertyUtils.value(bucket.properties(), "BucketName", YamlTree.class)
        .filter(bucketName -> TextUtils.isValue(bucketName, scalar.value()).isTrue())
        .isPresent();
    }
    return false;
  }

  private static boolean isReferringFunction(FunctionCallTree functionCall) {
    final var referringFunctions = Set.of("Ref", "Sub");
    return referringFunctions.contains(functionCall.name());
  }

  private static boolean isJoin(FunctionCallTree functionCall) {
    return functionCall.name().contains("Join");
  }

  private static String getNameOfBucket(Resource bucket) {
    return PropertyUtils.value(bucket.properties(), "BucketName")
      .flatMap(TextUtils::getValue)
      .orElse("");
  }

  /**
   * CloudFormation-specific resource value handling: supports {@code !Join} and {@code !Sub} intrinsic functions,
   * {@code SequenceTree}, and plain {@code ScalarTree}. Any other shape is conservatively treated as insecure.
   */
  private static boolean isInsecureResource(Tree resource) {
    if (resource instanceof FunctionCallTree functionCall && "Join".equals(functionCall.name()) && functionCall.arguments().size() == 2) {
      YamlTree listOfValues = functionCall.arguments().get(1);
      if (listOfValues instanceof SequenceTree sequence) {
        // The Join's effective suffix is the last element of its values list — that decides whether it ends with "*".
        return sequence.elements().isEmpty() || isInsecureResource(sequence.elements().get(sequence.elements().size() - 1));
      }
    } else if (resource instanceof FunctionCallTree functionCall && "Sub".equals(functionCall.name())) {
      // !Sub "arn:aws:s3:::${Bucket}/*" is secure if the template string ends with "*".
      return functionCall.arguments().stream()
        .map(TextUtils::getValue)
        .flatMap(Optional::stream)
        .noneMatch(arg -> arg.endsWith("*"));
    } else if (resource instanceof SequenceTree sequenceTree) {
      return sequenceTree.elements().stream().allMatch(BucketsInsecureHttpCheck::isInsecureResource);
    }
    return !(resource instanceof ScalarTree scalar && scalar.value().endsWith("*"));
  }
}
