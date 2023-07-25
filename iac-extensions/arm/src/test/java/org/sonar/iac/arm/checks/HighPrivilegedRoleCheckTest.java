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
package org.sonar.iac.arm.checks;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.of;
import static org.sonar.iac.arm.ArmTestUtils.readTemplateAndReplace;
import static org.sonar.iac.arm.checks.ArmVerifier.verifyContent;
import static org.sonar.iac.common.testing.Verifier.issue;

class HighPrivilegedRoleCheckTest {

  private static final HighPrivilegedRoleCheck CHECK = new HighPrivilegedRoleCheck();
  private static final String MESSAGE = "Make sure that assigning the %s role is safe here.";

  static Stream<Arguments> shouldCheckSecuritySensitiveRoles() {
    return Stream.of(
      of("b24988ac-6180-42a0-ab88-20f7382dd24c", "Contributor"),
      of("8e3af657-a8ff-443c-a75c-2fe8c4bcb635", "Owner"),
      of("18d7d88d-d35e-4fb5-a5c3-7773c20a72d9", "User Access Administrator"));
  }

  @MethodSource
  @ParameterizedTest(name = "[{index}] should check Security Sensitive role {1} ({0})")
  void shouldCheckSecuritySensitiveRoles(String role, String roleName) {
    String content = readTemplateAndReplace("HighPrivilegedRoleCheck/highPrivilegedRoleCheck.json", "${role}", role);

    verifyContent(content, CHECK,
      issue(12, 8, 12, 125, String.format(MESSAGE, roleName)),
      issue(23, 8, 23, 137, String.format(MESSAGE, roleName)),
      issue(34, 8, 34, 150, String.format(MESSAGE, roleName)),
      issue(45, 8, 45, 119, String.format(MESSAGE, roleName)),
      issue(56, 8, 56, 195, String.format(MESSAGE, roleName)),
      issue(67, 8, 67, 277, String.format(MESSAGE, roleName)));
  }

  static Stream<Arguments> shouldCheckSafeRoles() {
    return Stream.of(
      of("11111111-6180-42a0-ab88-20f7382dd24c"),
      of("00000000-1111-2222-3333-444444444444"),
      of("unknown"));
  }

  @MethodSource
  @ParameterizedTest(name = "[{index}] should check Security Sensitive role {1} ({0})")
  void shouldCheckSafeRoles(String role) {
    String content = readTemplateAndReplace("HighPrivilegedRoleCheck/highPrivilegedRoleCheck.json", "${role}", role);

    verifyContent(content, CHECK);
  }
}
