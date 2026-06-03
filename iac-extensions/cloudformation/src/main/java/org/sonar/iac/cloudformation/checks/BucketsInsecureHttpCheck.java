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
import org.sonar.iac.common.yaml.XPathUtils;
import org.sonar.iac.common.yaml.YamlTreeUtils;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6249")
public class BucketsInsecureHttpCheck implements IacCheck {
  private static final String MESSAGE = "No bucket policy enforces HTTPS-only access to this bucket.";
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
        .toList();
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

  private static class PolicyValidator {

    private static Map<YamlTree, String> getInsecureValues(Resource policy) {
      HashMap<YamlTree, String> result = new HashMap<>();

      YamlTree properties = policy.properties();
      if (properties != null) {
        List<YamlTree> statements = XPathUtils.getTrees(properties, "/PolicyDocument/Statement[]");

        // Short-circuit: if any statement fully enforces HTTPS, the policy is secure
        if (statements.stream().anyMatch(PolicyValidator::isSecureStatement)) {
          return result;
        }

        // Only Deny statements can be HTTPS enforcement — ignore Allow statements
        List<YamlTree> denyStatements = statements.stream()
          .filter(s -> XPathUtils.getSingleTree(s, "/Effect").filter(e -> !isInsecureEffect(e)).isPresent())
          .toList();

        // If no Deny statements exist, fall back to checking all statements (preserves
        // existing behavior for Allow-only or effectless policies)
        List<YamlTree> statementsToCheck = denyStatements.isEmpty() ? statements : denyStatements;

        statementsToCheck.forEach(statement -> {
          XPathUtils.getSingleTree(statement, "/Effect")
            .filter(PolicyValidator::isInsecureEffect).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_EFFECT));
          XPathUtils.getSingleTree(statement, "/Condition/Bool/aws:SecureTransport")
            .filter(PolicyValidator::isInsecureCondition).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_CONDITION));
          XPathUtils.getSingleTree(statement, "/Action")
            .filter(PolicyValidator::isInsecureAction).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_ACTION));
          XPathUtils.getSingleTree(statement, "/Principal")
            .filter(PolicyValidator::isInsecurePrincipal).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_PRINCIPAL));
          XPathUtils.getSingleTree(statement, "/Resource")
            .filter(PolicyValidator::isInsecureResource).ifPresent(t -> result.put(t, MESSAGE_SECONDARY_RESOURCE));
        });
      }

      return result;
    }

    private static boolean isSecureStatement(YamlTree statement) {
      return XPathUtils.getSingleTree(statement, "/Effect").filter(e -> !isInsecureEffect(e)).isPresent()
        && XPathUtils.getSingleTree(statement, "/Condition/Bool/aws:SecureTransport").filter(e -> !isInsecureCondition(e)).isPresent()
        && XPathUtils.getSingleTree(statement, "/Action").filter(e -> !isInsecureAction(e)).isPresent()
        && XPathUtils.getSingleTree(statement, "/Principal").filter(e -> !isInsecurePrincipal(e)).isPresent()
        && XPathUtils.getSingleTree(statement, "/Resource").filter(e -> !isInsecureResource(e)).isPresent();
    }

    private static boolean isInsecureResource(YamlTree resource) {
      if (resource instanceof FunctionCallTree functionCall && "Join".equals(functionCall.name()) && functionCall.arguments().size() == 2) {
        YamlTree listOfValues = functionCall.arguments().get(1);
        if (listOfValues instanceof SequenceTree sequence) {
          // Extract last element of Join value list to be checked if it's insecure
          return sequence.elements().isEmpty() || isInsecureResource(sequence.elements().get(sequence.elements().size() - 1));
        }
      } else if (resource instanceof FunctionCallTree functionCall && "Sub".equals(functionCall.name())) {
        // !Sub "arn:aws:s3:::${Bucket}/*" is secure if the template string ends with *
        return functionCall.arguments().stream()
          .map(TextUtils::getValue)
          .flatMap(Optional::stream)
          .noneMatch(arg -> arg.endsWith("*"));
      } else if (resource instanceof SequenceTree sequenceTree) {
        return sequenceTree.elements().stream().allMatch(PolicyValidator::isInsecureResource);
      }
      return !(resource instanceof ScalarTree scalar && scalar.value().endsWith("*"));
    }

    private static boolean isInsecurePrincipal(YamlTree principal) {
      Tree valueToCheck = principal;
      if (principal instanceof MappingTree) {
        valueToCheck = PropertyUtils.valueOrNull(principal, "AWS");
      }
      if (valueToCheck instanceof SequenceTree sequenceTree) {
        return sequenceTree.elements().stream().allMatch(PolicyValidator::isInsecurePrincipal);
      }
      return !TextUtils.isValue(valueToCheck, "*").isTrue();
    }

    private static boolean isInsecureAction(YamlTree action) {
      if (action instanceof SequenceTree sequenceTree) {
        return sequenceTree.elements().stream().allMatch(PolicyValidator::isInsecureAction);
      }
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
