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
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.checks.AbstractResourceCheck.Resource;
import org.sonar.iac.cloudformation.checks.utils.XPathUtils;
import org.sonar.iac.cloudformation.tree.FunctionCallTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.YamlTreeUtils;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

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
    Map<YamlTree, String> insecureValues = PolicyValidator.getInsecureValues(policy);
    if (!insecureValues.isEmpty()) {
      List<SecondaryLocation> secondaryLocations = insecureValues.entrySet().stream()
        .map(e -> new SecondaryLocation(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
      ctx.reportIssue(bucket.type(), MESSAGE, secondaryLocations);
    }
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
    if (policyBucketId instanceof FunctionCallTree && isReferringFunction((FunctionCallTree) policyBucketId)) {
      return ((FunctionCallTree) policyBucketId).arguments().stream()
        .map(TextUtils::getValue)
        .flatMap(Optional::stream)
        .anyMatch(argument -> TextUtils.isValue(bucket.name(), argument).isTrue());
    } else if (policyBucketId instanceof FunctionCallTree && isJoin((FunctionCallTree) policyBucketId)) {
      return ((FunctionCallTree) policyBucketId).arguments().stream()
        .map(YamlTreeUtils::getListValueElements)
        .flatMap(Collection::stream)
        // Remove the empty strings before checking for equality, as the bucket name can also be an empty string
        .filter(elem -> !"".equals(elem))
        .anyMatch(elementValue -> getNameOfBucket(bucket).equals(elementValue));
    } else if (policyBucketId instanceof ScalarTree) {
      return PropertyUtils.value(bucket.properties(), "BucketName", YamlTree.class)
        .filter(bucketName -> TextUtils.isValue(bucketName, ((ScalarTree) policyBucketId).value()).isTrue())
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

  private static class PolicyValidator {

    private static Map<YamlTree, String> getInsecureValues(Resource policy) {
      HashMap<YamlTree, String> result = new HashMap<>();

      YamlTree properties = policy.properties();
      if (properties != null) {
        Optional<YamlTree> statement = XPathUtils.getSingleTree(properties, "/PolicyDocument/Statement[]");
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
      }

      return result;
    }

    private static boolean isInsecureResource(YamlTree resource) {
      if (resource instanceof FunctionCallTree && "Join".equals(((FunctionCallTree) resource).name()) && (((FunctionCallTree) resource).arguments().size() == 2)) {
        FunctionCallTree joinCall = (FunctionCallTree) resource;
        YamlTree listOfValues = joinCall.arguments().get(1);
        if (listOfValues instanceof SequenceTree) {
          SequenceTree sequence = (SequenceTree) listOfValues;
          // Extract last element of Join value list to be checked if it's insecure
          return sequence.elements().isEmpty() || isInsecureResource(sequence.elements().get(sequence.elements().size() - 1));
        }
      }
      return !(resource instanceof ScalarTree && ((ScalarTree) resource).value().endsWith("*"));
    }

    private static boolean isInsecurePrincipal(YamlTree principal) {
      Tree valueToCheck = principal;
      if (principal instanceof MappingTree) {
        valueToCheck = PropertyUtils.valueOrNull(principal, "AWS");
      }
      return !TextUtils.isValue(valueToCheck, "*").isTrue();
    }

    private static boolean isInsecureAction(YamlTree action) {
      return !(TextUtils.isValue(action, "*").isTrue() || TextUtils.isValue(action, "s3:*").isTrue());
    }

    private static boolean isInsecureCondition(YamlTree condition) {
      return TextUtils.isValue(condition, "true").isTrue();
    }

    private static boolean isInsecureEffect(YamlTree effect) {
      return TextUtils.isValue(effect, "Allow").isTrue();
    }
  }
}
