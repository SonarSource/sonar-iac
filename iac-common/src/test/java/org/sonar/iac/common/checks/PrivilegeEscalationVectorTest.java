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
package org.sonar.iac.common.checks;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.policy.Policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.CommonTestUtils.TestAttributeTree.attribute;
import static org.sonar.iac.common.checks.CommonTestUtils.TestPropertiesTree.properties;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTextTree.text;
import static org.sonar.iac.common.checks.PrivilegeEscalationVector.actionEnablesVector;
import static org.sonar.iac.common.checks.PrivilegeEscalationVector.getActionEscalationVector;
import static org.sonar.iac.common.checks.PrivilegeEscalationVector.getStatementEscalationVector;

class PrivilegeEscalationVectorTest {

  @Test
  void escalationVectorApply_on_single_sensitive_permission() {
    assertThat(getActionEscalationVector(List.of(text("iam:CreateAccessKey")))).isPresent();
  }

  @Test
  void escalationVectorApply_on_single_compliant_permission() {
    assertThat(getActionEscalationVector(List.of(text("iam:bar")))).isEmpty();
  }

  @Test
  void escalationVectorApply_on_single_sensitive_wildcard_permission() {
    assertThat(getActionEscalationVector(List.of(text("ec2:*")))).isEmpty();
  }

  @Test
  void escalationVectorApply_on_single_compliant_wildcard_permission() {
    assertThat(getActionEscalationVector(List.of(text("iam:*")))).isPresent();
  }

  @Test
  void escalationVectorApply_on_multiple_permissions() {
    assertThat(getActionEscalationVector(List.of(text("glue:foo"), text("iam:CreateAccessKey")))).isPresent();
  }

  @Test
  void escalationVectorApply_on_multiple_sensitive_permission() {
    assertThat(getActionEscalationVector(List.of(text("iam:UpdateAssumeRolePolicy"), text("sts:AssumeRole")))).isPresent();
  }

  @Test
  void escalationVectorApply_on_multiple_sensitive_permission_with_wildcard() {
    assertThat(getActionEscalationVector(List.of(text("iam:*"), text("sts:AssumeRole")))).isPresent();
  }

  @Test
  void escalationVectorApply_unexpected_permission_format() {
    assertThat(getActionEscalationVector(List.of(text("foo")))).isEmpty();
  }

  @Test
  void all_vectors_have_a_non_empty_name_and_permissions_list() {
    assertThat(PrivilegeEscalationVector.values()).allSatisfy(vector -> {
      assertThat(vector.getName()).isNotEmpty();
      assertThat(vector.getPermissions()).isNotEmpty();
    });
  }

  @Test
  void actionValueEnablesTheGivenVector() {
    assertThat(actionEnablesVector(PrivilegeEscalationVector.EC2, "iam:PassRole")).isTrue();
    assertThat(actionEnablesVector(PrivilegeEscalationVector.EC2, "failMe")).isFalse();
  }

  @Test
  void getStatementEscalationVectorSuccess() {
    Policy.Statement statement = statement(Map.of("Effect", "Allow",
      "Action", "action",
      "Resource", "*"));

    Optional<PrivilegeEscalationVector> actualVector = getStatementEscalationVector(statement, List.of(text("iam" +
      ":CreatePolicyVersion")));
    assertThat(actualVector).containsSame(PrivilegeEscalationVector.CREATE_POLICY_VERSION);
  }

  @Test
  void getStatementEscalationVectorNoAllowEffect() {
    Policy.Statement statement = statement(Map.of());

    Optional<PrivilegeEscalationVector> actualVector = getStatementEscalationVector(statement, List.of(text("iam" +
      ":CreatePolicyVersion")));
    assertThat(actualVector).isEmpty();
  }

  @Test
  void getStatementEscalationVectorWithResourceNotSensitive() {
    Policy.Statement statement = statement(Map.of("Effect", "Allow",
      "Action", "action",
      "Resource", "NotSensitive"));

    Optional<PrivilegeEscalationVector> actualVector = getStatementEscalationVector(statement, List.of(text("iam" +
      ":CreatePolicyVersion")));
    assertThat(actualVector).isEmpty();
  }

  @Test
  void getStatementEscalationVectorWithResourceInPattern() {
    Policy.Statement statement = statement(Map.of("Effect", "Allow",
      "Action", "action",
      "Resource", "arn:foo:bar:baz:bax:user/*"));

    Optional<PrivilegeEscalationVector> actualVector = getStatementEscalationVector(statement, List.of(text("iam" +
      ":CreatePolicyVersion")));
    assertThat(actualVector).containsSame(PrivilegeEscalationVector.CREATE_POLICY_VERSION);
  }

  @Test
  void getStatementEscalationVectorConditionExists() {
    Policy.Statement statement = statement(Map.of("Effect", "Allow",
      "Action", "action",
      "Resource", "*",
      "Condition", "condition"));

    Optional<PrivilegeEscalationVector> actualVector = getStatementEscalationVector(statement, List.of(text("iam" +
      ":CreatePolicyVersion")));
    assertThat(actualVector).isEmpty();
  }

  @Test
  void getStatementEscalationVectorPrincipalExists() {
    Policy.Statement statement = statement(Map.of("Effect", "Allow",
      "Action", "action",
      "Resource", "*",
      "Principal", "principal"));

    Optional<PrivilegeEscalationVector> actualVector = getStatementEscalationVector(statement, List.of(text("iam" +
      ":CreatePolicyVersion")));
    assertThat(actualVector).isEmpty();
  }

  @Test
  void getStatementEscalationVectorActionNotPresent() {
    Policy.Statement statement = statement(Map.of("Effect", "Allow",
      "Resource", "*"));

    Optional<PrivilegeEscalationVector> actualVector = getStatementEscalationVector(statement, List.of(text("iam" +
      ":CreatePolicyVersion")));
    assertThat(actualVector).isEmpty();
  }

  private static Policy.Statement statement(Map<String, String> properties) {
    PropertyTree[] propertyTrees = properties.entrySet().stream()
      .map(e -> attribute(e.getKey(), text(e.getValue())))
      .toArray(PropertyTree[]::new);
    CommonTestUtils.TestPropertiesTree statement = properties(propertyTrees);
    return new Policy(null, tree -> List.of(statement)).statement().get(0);
  }
}
