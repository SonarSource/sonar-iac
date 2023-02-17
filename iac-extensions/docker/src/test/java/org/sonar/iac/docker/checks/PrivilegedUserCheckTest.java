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
package org.sonar.iac.docker.checks;

import org.junit.jupiter.api.Test;

class PrivilegedUserCheckTest {

  private final PrivilegedUserCheck check = new PrivilegedUserCheck();

  @Test
  void testNonCompliant() {
    for (int i = 0; i < 14; i++) {
      DockerVerifier.verify("PrivilegedUserCheck/Dockerfile." + i, check);
    }
  }

  @Test
  void testCompliant() {
    for (int i = 0; i < 9; i++) {
      DockerVerifier.verifyNoIssue("PrivilegedUserCheck/Dockerfile-Compliant." + i, check);
    }
  }

  @Test
  void testCustomSafeList() {
    check.safeImages = "custom_image1, custom_image2, golang";
    for (int i = 0; i < 3; i++) {
      DockerVerifier.verify("PrivilegedUserCheck/Dockerfile_customSafeImages." + i, check);
    }
  }

  @Test
  void testCustomSafeListCompliant() {
    check.safeImages = "custom_image1, custom_image2, golang";
    for (int i = 0; i < 7; i++) {
      DockerVerifier.verifyNoIssue("PrivilegedUserCheck/Dockerfile_customSafeImages-Compliant." + i, check);
    }
  }

  @Test
  void testMultiStageBuild() {
    DockerVerifier.verify("PrivilegedUserCheck/Dockerfile_multi_stage_build", check);
  }
  @Test
  void testMultiStageBuildCompliant() {
    DockerVerifier.verifyNoIssue("PrivilegedUserCheck/Dockerfile_multi_stage_build-Compliant", check);
  }
}
