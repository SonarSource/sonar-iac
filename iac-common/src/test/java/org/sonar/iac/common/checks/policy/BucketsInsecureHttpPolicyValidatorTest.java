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

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.CommonTestUtils.TestPropertiesTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.CommonTestUtils.TestAttributeTree.attribute;
import static org.sonar.iac.common.checks.CommonTestUtils.TestIterable.list;
import static org.sonar.iac.common.checks.CommonTestUtils.TestPropertiesTree.properties;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTextTree.text;

class BucketsInsecureHttpPolicyValidatorTest {

  private static final Predicate<Tree> NEVER_INSECURE_RESOURCE = t -> false;
  private static final Predicate<Tree> ALWAYS_INSECURE_RESOURCE = t -> true;

  private final BucketsInsecureHttpPolicyValidator validator = new BucketsInsecureHttpPolicyValidator(NEVER_INSECURE_RESOURCE);

  @Test
  void shouldTreatEmptyPolicyAsSecureToAvoidFalsePositives() {
    Policy policy = new Policy(null, null, Collections.emptyList());
    assertThat(validator.isPolicySecure(policy)).isTrue();
    assertThat(validator.findInsecureFields(policy)).isEmpty();
  }

  @Test
  void shouldTreatUnreasonableStatementsAsSecure() {
    // Statement has none of effect/condition/action/principal/resource — we cannot reason about it.
    PropertyTree unrelated = attribute("SomethingElse", "value");
    Policy policy = policyWith(properties(unrelated));

    assertThat(validator.isPolicySecure(policy)).isTrue();
    assertThat(validator.findInsecureFields(policy)).isEmpty();
  }

  @Test
  void shouldReportSecureStatementWhenAllFieldsRestrictiveAndConditionFalse() {
    Policy policy = policyWith(secureStatement());
    assertThat(validator.isPolicySecure(policy)).isTrue();
    assertThat(validator.findInsecureFields(policy)).isEmpty();
  }

  @Test
  void shouldReportInsecureWhenEffectIsAllow() {
    Tree allowEffect = text("Allow");
    Policy policy = policyWith(properties(
      attribute("Effect", allowEffect),
      attribute("Principal", text("*")),
      attribute("Action", text("s3:*")),
      attribute("Resource", text("arn:aws:s3:::bucket/*")),
      attribute("Condition", secureTransportFalseCondition())));

    assertThat(validator.isPolicySecure(policy)).isFalse();
    assertThat(validator.findInsecureFields(policy))
      .containsEntry(allowEffect, BucketsInsecureHttpPolicyValidator.MESSAGE_SECONDARY_EFFECT);
  }

  @Test
  void shouldReportInsecureWhenActionIsNarrow() {
    Tree narrowAction = text("s3:GetObject");
    Policy policy = policyWith(properties(
      attribute("Effect", text("Deny")),
      attribute("Principal", text("*")),
      attribute("Action", narrowAction),
      attribute("Resource", text("arn:aws:s3:::bucket/*")),
      attribute("Condition", secureTransportFalseCondition())));

    assertThat(validator.isPolicySecure(policy)).isFalse();
    assertThat(validator.findInsecureFields(policy))
      .containsEntry(narrowAction, BucketsInsecureHttpPolicyValidator.MESSAGE_SECONDARY_ACTION);
  }

  @Test
  void shouldReportInsecureWhenPrincipalIsArn() {
    Tree arnPrincipal = text("arn:aws:iam::123:root");
    Policy policy = policyWith(properties(
      attribute("Effect", text("Deny")),
      attribute("Principal", arnPrincipal),
      attribute("Action", text("s3:*")),
      attribute("Resource", text("arn:aws:s3:::bucket/*")),
      attribute("Condition", secureTransportFalseCondition())));

    assertThat(validator.isPolicySecure(policy)).isFalse();
    assertThat(validator.findInsecureFields(policy))
      .containsEntry(arnPrincipal, BucketsInsecureHttpPolicyValidator.MESSAGE_SECONDARY_PRINCIPAL);
  }

  @Test
  void shouldReportInsecureWhenResourcePredicateFlagsIt() {
    Tree resource = text("arn:aws:s3:::bucket/sensitive");
    Policy policy = policyWith(properties(
      attribute("Effect", text("Deny")),
      attribute("Principal", text("*")),
      attribute("Action", text("s3:*")),
      attribute("Resource", resource),
      attribute("Condition", secureTransportFalseCondition())));
    var strictValidator = new BucketsInsecureHttpPolicyValidator(ALWAYS_INSECURE_RESOURCE);

    assertThat(strictValidator.isPolicySecure(policy)).isFalse();
    assertThat(strictValidator.findInsecureFields(policy))
      .containsEntry(resource, BucketsInsecureHttpPolicyValidator.MESSAGE_SECONDARY_RESOURCE);
  }

  @Test
  void shouldNotEmitSecondariesWhenStatementIsNotHttpsOnlyAttempt() {
    // SONARIAC-1803: a Deny statement whose Condition does NOT contain aws:SecureTransport == "false"
    // is not an HTTPS-only attempt. The policy is insecure (no HTTPS-only enforcement) but per-field
    // secondaries (Action, Principal, etc.) would be misleading and must not be emitted.
    Tree narrowAction = text("s3:GetObject");
    Tree arnPrincipal = text("arn:aws:iam::123:root");
    Policy policy = policyWith(properties(
      attribute("Effect", text("Deny")),
      attribute("Principal", arnPrincipal),
      attribute("Action", narrowAction),
      attribute("Resource", text("arn:aws:s3:::bucket/*")),
      attribute("Condition", properties(
        attribute("IpAddress", properties(attribute("aws:SourceIp", "1.2.3.4/32")))))));

    assertThat(validator.isPolicySecure(policy)).isFalse();
    assertThat(validator.findInsecureFields(policy)).isEmpty();
  }

  @Test
  void shouldRaiseWithoutSecondariesForHttpsOnlyAttemptMissingRequiredFields() {
    // A statement that carries the aws:SecureTransport: false condition counts as an HTTPS-only attempt
    // (isReasonableStatement is true via the condition; isHttpsOnlyAttempt is true), but it lacks
    // Effect/Action/Principal/Resource entirely. isSecureStatement therefore returns false (the field
    // filters all short-circuit), so the policy is insecure — but findInsecureFields has nothing to
    // point at either (statement.effect().filter(...) is empty when the field is absent). The contract
    // is intentional: primary issue is raised with no misleading per-field hints.
    Policy policy = policyWith(properties(
      attribute("Condition", secureTransportFalseCondition())));

    assertThat(validator.isPolicySecure(policy)).isFalse();
    assertThat(validator.findInsecureFields(policy)).isEmpty();
  }

  @Test
  void shouldNotEmitSecondariesWhenSecureTransportIsTrue() {
    // Condition value "true" inverts the protection — statement is no longer an HTTPS-only attempt.
    Tree narrowAction = text("s3:GetObject");
    Policy policy = policyWith(properties(
      attribute("Effect", text("Deny")),
      attribute("Principal", text("*")),
      attribute("Action", narrowAction),
      attribute("Resource", text("arn:aws:s3:::bucket/*")),
      attribute("Condition", properties(
        attribute("Bool", properties(attribute("aws:SecureTransport", "true")))))));

    assertThat(validator.isPolicySecure(policy)).isFalse();
    assertThat(validator.findInsecureFields(policy)).isEmpty();
  }

  @Test
  void shouldShortCircuitWhenAtLeastOneStatementIsSecure() {
    // The secure statement makes the whole policy secure even if a sibling Allow statement looks shaky.
    PropertyTree siblingAllow = attribute("Statement", properties(
      attribute("Effect", text("Allow")),
      attribute("Action", text("s3:PutObject")),
      attribute("Resource", text("arn:aws:s3:::bucket/log/*")),
      attribute("Principal", properties(attribute("Service", "logging.s3.amazonaws.com")))));

    Tree root = properties(
      attribute("Statement", secureStatement()),
      siblingAllow);

    Policy policy = new Policy(null, null, root.children().stream().map(Policy.Statement::new).toList());

    assertThat(validator.isPolicySecure(policy)).isTrue();
  }

  // --- predicate-level checks ---

  @Test
  void isInsecureEffect() {
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureEffect(text("Allow"))).isTrue();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureEffect(text("Deny"))).isFalse();
    // SONARIAC-1804: unresolved / unusual values are NOT flagged as insecure.
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureEffect(text("AllowSomething"))).isFalse();
  }

  @Test
  void isInsecureActionForScalar() {
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureAction(text("*"))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureAction(text("s3:*"))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureAction(text("s3:GetObject"))).isTrue();
  }

  @Test
  void isInsecureActionForIterable() {
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureAction(list(text("s3:*")))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureAction(list(text("s3:GetObject"), text("s3:PutObject")))).isTrue();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureAction(list(text("s3:GetObject"), text("*")))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecureAction(list())).isTrue();
  }

  @Test
  void isInsecurePrincipalForMapping() {
    // Mapping form — only the AWS sub-key is inspected.
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipal(
      properties(attribute("AWS", "*")))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipal(
      properties(attribute("AWS", "arn:aws:iam::123:root")))).isTrue();
    // Missing AWS key — not flagged (matches pre-existing TF behaviour).
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipal(
      properties(attribute("Service", "logging.s3.amazonaws.com")))).isFalse();
  }

  @Test
  void isInsecurePrincipalForNonMapping() {
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipal(text("*"))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipal(text("arn:aws:iam::123:root"))).isTrue();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipal(list(text("*")))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipal(list(text("arn:aws:iam::123:root")))).isTrue();
  }

  @Test
  void isInsecurePrincipalValueScalarIsLenient() {
    // Scalar with non-text value (no value to compare against) stays secure to avoid false positives.
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipalValue(properties())).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipalValue(text("*"))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipalValue(text("arn:..."))).isTrue();
  }

  @Test
  void isInsecurePrincipalValueIterableIsStrict() {
    // Inside a list, only DEFINITELY-"*" elements are secure; unresolved entries flag a coverage gap.
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipalValue(list(text("*")))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipalValue(list(text("arn:..."), text("*")))).isFalse();
    assertThat(BucketsInsecureHttpPolicyValidator.isInsecurePrincipalValue(list(text("arn:...")))).isTrue();
  }

  // --- helpers ---

  /**
   * Build a Policy whose Statement is the given properties tree (single statement).
   * The statement provider yields the inner statement tree directly.
   */
  private static Policy policyWith(TestPropertiesTree statement) {
    return new Policy(null, null, List.of(new Policy.Statement(statement)));
  }

  private static TestPropertiesTree secureStatement() {
    return properties(
      attribute("Effect", text("Deny")),
      attribute("Principal", text("*")),
      attribute("Action", text("s3:*")),
      attribute("Resource", text("arn:aws:s3:::bucket/*")),
      attribute("Condition", secureTransportFalseCondition()));
  }

  private static TestPropertiesTree secureTransportFalseCondition() {
    return properties(attribute("Bool", properties(attribute("aws:SecureTransport", "false"))));
  }
}
