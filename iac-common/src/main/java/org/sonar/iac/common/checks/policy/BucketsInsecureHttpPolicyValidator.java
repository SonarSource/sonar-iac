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
package org.sonar.iac.common.checks.policy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

/**
 * Shared rule logic for S6249 ({@code BucketsInsecureHttpCheck}) across CloudFormation and Terraform.
 *
 * <p>Operates on the language-agnostic {@link Policy} model. The only language-specific concern
 * — recognising secure resource ARNs (Sub/Join, template expressions, etc.) — is injected as a predicate.
 */
public final class BucketsInsecureHttpPolicyValidator {

  public static final String MESSAGE = "No bucket policy enforces HTTPS-only access to this bucket.";
  public static final String MESSAGE_SECONDARY_EFFECT = "Non-conforming requests should be denied.";
  public static final String MESSAGE_SECONDARY_ACTION = "All S3 actions should be restricted.";
  public static final String MESSAGE_SECONDARY_PRINCIPAL = "All principals should be restricted.";
  public static final String MESSAGE_SECONDARY_RESOURCE = "All resources should be restricted.";

  private final Predicate<Tree> isInsecureResource;

  /**
   * @param isInsecureResource language-specific predicate that decides whether a {@code Resource} field value is insecure
   *                           (i.e. does not cover {@code arn:...:*}). Receives the raw Statement.resource() tree.
   */
  public BucketsInsecureHttpPolicyValidator(Predicate<Tree> isInsecureResource) {
    this.isInsecureResource = isInsecureResource;
  }

  /**
   * A policy is secure when at least one statement is a valid HTTPS-only enforcement attempt
   * (carries the {@code aws:SecureTransport: false} condition) that restricts every relevant field.
   *
   * <p>If the policy has no statements we can reason about (e.g. {@code file("policy.json")},
   * unresolved data references, malformed JSON, intrinsic functions returning Statement) we treat it
   * as secure to avoid false positives.
   */
  public boolean isPolicySecure(Policy policy) {
    var reasonable = policy.statement().stream()
      .filter(BucketsInsecureHttpPolicyValidator::isReasonableStatement)
      .toList();
    if (reasonable.isEmpty()) {
      return true;
    }
    return reasonable.stream().anyMatch(this::isSecureStatement);
  }

  /**
   * Collect the insecure fields that explain why this policy is not enforcing HTTPS-only.
   * Per-field secondary messages are only emitted for statements that are actual HTTPS-only enforcement
   * attempts (carry an {@code aws:SecureTransport: false} condition). When no such statement exists the
   * caller still reports the primary issue, but with no secondary locations.
   */
  public Map<Tree, String> findInsecureFields(Policy policy) {
    Map<Tree, String> result = new HashMap<>();
    policy.statement().stream()
      .filter(BucketsInsecureHttpPolicyValidator::isHttpsOnlyAttempt)
      .forEach(statement -> {
        statement.effect().filter(BucketsInsecureHttpPolicyValidator::isInsecureEffect)
          .ifPresent(effect -> result.put(effect, MESSAGE_SECONDARY_EFFECT));

        statement.action().filter(BucketsInsecureHttpPolicyValidator::isInsecureAction)
          .ifPresent(action -> result.put(action, MESSAGE_SECONDARY_ACTION));

        statement.principal().filter(BucketsInsecureHttpPolicyValidator::isInsecurePrincipal)
          .ifPresent(principal -> result.put(principal, MESSAGE_SECONDARY_PRINCIPAL));

        statement.resource().filter(isInsecureResource)
          .ifPresent(resource -> result.put(resource, MESSAGE_SECONDARY_RESOURCE));
      });
    return result;
  }

  /**
   * A statement we can reason about must expose at least one of the fields S6249 inspects.
   * Statements pulled from data references or unresolved expressions present as empty Statement records
   * and would otherwise be misclassified as insecure.
   */
  private static boolean isReasonableStatement(Policy.Statement s) {
    return s.effect().isPresent() || s.condition().isPresent()
      || s.action().isPresent() || s.principal().isPresent() || s.resource().isPresent();
  }

  /**
   * A statement is an HTTPS-only enforcement attempt iff its Condition contains
   * {@code Bool.aws:SecureTransport == "false"}.
   */
  private static boolean isHttpsOnlyAttempt(Policy.Statement statement) {
    return statement.condition()
      .flatMap(c -> PropertyUtils.value(c, "Bool"))
      .flatMap(b -> PropertyUtils.value(b, "aws:SecureTransport"))
      .filter(TextUtils::isValueFalse)
      .isPresent();
  }

  private boolean isSecureStatement(Policy.Statement statement) {
    return isHttpsOnlyAttempt(statement)
      && statement.effect().filter(e -> !isInsecureEffect(e)).isPresent()
      && statement.action().filter(e -> !isInsecureAction(e)).isPresent()
      && statement.principal().filter(e -> !isInsecurePrincipal(e)).isPresent()
      && statement.resource().filter(e -> !isInsecureResource.test(e)).isPresent();
  }

  /**
   * Effect is insecure only when explicitly {@code "Allow"} (CFN-style; matches the AWS IAM spec which
   * requires Effect — absent or unresolved values are treated as malformed-but-unknown rather than insecure).
   */
  public static boolean isInsecureEffect(Tree effect) {
    return TextUtils.isValue(effect, "Allow").isTrue();
  }

  public static boolean isInsecureAction(Tree action) {
    if (action instanceof Iterable<?>) {
      return iterableElements(action).allMatch(BucketsInsecureHttpPolicyValidator::isInsecureAction);
    }
    return TextUtils.isValue(action, "*").isFalse() && TextUtils.isValue(action, "s3:*").isFalse();
  }

  public static boolean isInsecurePrincipal(Tree principal) {
    // Mapping form (`{ AWS = ... }`): only the AWS sub-key applies to S3 HTTPS-only enforcement.
    if (principal instanceof HasProperties) {
      return PropertyUtils.value(principal, "AWS")
        .filter(BucketsInsecureHttpPolicyValidator::isInsecurePrincipalValue)
        .isPresent();
    }
    return isInsecurePrincipalValue(principal);
  }

  /**
   * Insecure-principal-value check used for both the bare {@code Principal} value and the value of the AWS sub-key.
   *
   * <p>Scalar values are evaluated leniently: only DEFINITELY-not-"*" is insecure, unresolved values stay
   * compliant to avoid false positives on templates. List elements are evaluated strictly: an element is
   * secure only when it is DEFINITELY "*" — unresolved entries flag a coverage gap.
   */
  public static boolean isInsecurePrincipalValue(Tree value) {
    if (value instanceof Iterable<?>) {
      return iterableElements(value).allMatch(e -> !TextUtils.isValue(e, "*").isTrue());
    }
    return TextUtils.isValue(value, "*").isFalse();
  }

  /**
   * Stream the elements of any iterable-shaped tree node. Used by predicates that need to recurse over
   * array/sequence/tuple shapes from any language.
   */
  static Stream<Tree> iterableElements(Tree tree) {
    if (tree instanceof Iterable<?> iterable) {
      return StreamSupport.stream(iterable.spliterator(), false)
        .filter(Tree.class::isInstance)
        .map(Tree.class::cast);
    }
    return Stream.empty();
  }
}
