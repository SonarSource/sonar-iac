/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class HighPrivilegedRoleCheckTest {

  private static final HighPrivilegedRoleCheck CHECK = new HighPrivilegedRoleCheck();
  private static final String MESSAGE = "Make sure that assigning the %s role is safe here.";

  static Stream<Arguments> listSecuritySensitiveRoles() {
    return Stream.of(
      of("b24988ac-6180-42a0-ab88-20f7382dd24c", "Contributor"),
      of("8e3af657-a8ff-443c-a75c-2fe8c4bcb635", "Owner"),
      of("18d7d88d-d35e-4fb5-a5c3-7773c20a72d9", "User Access Administrator"));
  }

  @MethodSource("listSecuritySensitiveRoles")
  @ParameterizedTest(name = "[{index}] should check Security Sensitive role {1} ({0})")
  void shouldCheckSecuritySensitiveRolesJson(String role, String roleName) {
    String content = readTemplateAndReplace("HighPrivilegedRoleCheck/highPrivilegedRoleCheck.json", "${role}", role);

    ArmVerifier.verifyContent(content, CHECK,
      issue(13, 8, 13, 125, String.format(MESSAGE, roleName)),
      issue(24, 8, 24, 137, String.format(MESSAGE, roleName)),
      issue(35, 8, 35, 150, String.format(MESSAGE, roleName)),
      issue(46, 8, 46, 119, String.format(MESSAGE, roleName)),
      issue(57, 8, 57, 183, String.format(MESSAGE, roleName)),
      issue(68, 8, 68, 265, String.format(MESSAGE, roleName)));
  }

  @MethodSource("listSecuritySensitiveRoles")
  @ParameterizedTest(name = "[{index}] should check Security Sensitive role {1} ({0})")
  void shouldCheckSecuritySensitiveRolesBicep(String role, String roleName) {
    String content = readTemplateAndReplace("HighPrivilegedRoleCheck/highPrivilegedRoleCheck.bicep", "${role}", role, "${roleName}", roleName);

    BicepVerifier.verifyContent(content, CHECK);
  }

  static Stream<Arguments> listSafeRoles() {
    return Stream.of(
      of("11111111-6180-42a0-ab88-20f7382dd24c"),
      of("00000000-1111-2222-3333-444444444444"),
      of("unknown"));
  }

  @MethodSource("listSafeRoles")
  @ParameterizedTest(name = "[{index}] should check Security Sensitive role ({0})")
  void shouldCheckSafeRolesJson(String role) {
    String content = readTemplateAndReplace("HighPrivilegedRoleCheck/highPrivilegedRoleCheck.json", "${role}", role);

    ArmVerifier.verifyContent(content, CHECK);
  }

  @MethodSource("listSafeRoles")
  @ParameterizedTest(name = "[{index}] should check Security Sensitive role {1} ({0})")
  void shouldCheckSafeRolesBicep(String role) {
    String content = readTemplateAndReplace("HighPrivilegedRoleCheck/highPrivilegedRoleCheck.bicep", "${role}", role);

    BicepVerifier.verifyContentNoIssue(content, CHECK);
  }
}
