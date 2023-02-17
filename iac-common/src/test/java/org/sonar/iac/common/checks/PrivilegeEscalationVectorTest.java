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

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.PrivilegeEscalationVector.isSupersetOfAnEscalationVector;

class PrivilegeEscalationVectorTest {

  @Test
  void escalationVectorApply_on_single_sensitive_permission() {
    assertThat(isSupersetOfAnEscalationVector(Stream.of("iam:CreateAccessKey"))).isTrue();
  }

  @Test
  void escalationVectorApply_on_single_compliant_permission() {
    assertThat(isSupersetOfAnEscalationVector(Stream.of("iam:bar"))).isFalse();
  }

  @Test
  void escalationVectorApply_on_single_sensitive_wildcard_permission() {
    assertThat(isSupersetOfAnEscalationVector(Stream.of("ec2:*"))).isFalse();
  }

  @Test
  void escalationVectorApply_on_single_compliant_wildcard_permission() {
    assertThat(isSupersetOfAnEscalationVector(Stream.of("iam:*"))).isTrue();
  }

  @Test
  void escalationVectorApply_on_multiple_permissions() {
    assertThat(isSupersetOfAnEscalationVector(Stream.of("glue:foo", "iam:CreateAccessKey"))).isTrue();
  }

  @Test
  void escalationVectorApply_on_multiple_sensitive_permission() {
    assertThat(isSupersetOfAnEscalationVector(Stream.of("iam:UpdateAssumeRolePolicy", "sts:AssumeRole"))).isTrue();
  }

  @Test
  void escalationVectorApply_on_multiple_sensitive_permission_with_wildcard() {
    assertThat(isSupersetOfAnEscalationVector(Stream.of("iam:*", "sts:AssumeRole"))).isTrue();
  }

  @Test
  void escalationVectorApply_unexpected_permission_format() {
    assertThat(isSupersetOfAnEscalationVector(Stream.of("foo"))).isFalse();
  }
}
