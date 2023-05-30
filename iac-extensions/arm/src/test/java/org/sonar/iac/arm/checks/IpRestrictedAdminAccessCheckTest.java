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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.testing.Verifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.checks.ArmVerifier.issues;
import static org.sonar.iac.arm.checks.ArmVerifier.verifyNoIssue;

class IpRestrictedAdminAccessCheckTest {

  private static final String PATH_PREFIX = "IpRestrictedAdminAccessCheck/";
  private static final IpRestrictedAdminAccessCheck CHECK = new IpRestrictedAdminAccessCheck();

  private static List<String> shouldRaiseIssue() {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      result.add("securityRules-positives-" + i + ".json");
    }
    return result;
  }

  private static List<String> shouldNotRaiseIssue() {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      result.add("securityRules-negatives-" + i + ".json");
    }
    return result;
  }

  @MethodSource
  @ParameterizedTest(name = "should raise issue for {0}")
  void shouldRaiseIssue(String filename) {
    List<Verifier.Issue> issues = issues(PATH_PREFIX + filename, CHECK);
    for (Verifier.Issue issue : issues) {
      System.out.println(issue);
      assertThat(issue.getMessage()).isEqualTo("Restrict IP addresses authorized to access administration services");
      assertThat(issue.getTextRange()).startsAt(10,8);
      assertThat(issue.getSecondaryLocations()).isEmpty();
    }
  }

  @MethodSource
  @ParameterizedTest(name = "should NOT raise issue for {0}")
  void shouldNotRaiseIssue(String filename) {
    verifyNoIssue(PATH_PREFIX + filename, CHECK);
  }
}
