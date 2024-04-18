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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

class RedundantResourceDependenciesCheckTest {
  @Test
  void shouldRaiseIssuesInJson() {
    // TODO SONARIAC-1426: S6952: Cover `*ResourceId` functions in the `dependsOn` block
    // should also raise on line 11 (`dependsOn` with `resourceId`). On line 13 there is `dependsOn` with more complex function,
    // which we can't resolve, because it's dynamic, so it's a known FN.
    ArmVerifier.verify("RedundantResourceDependenciesCheck/test.json", new RedundantResourceDependenciesCheck(),
      Verifier.issue(12, 8, 12, 27, "Remove this explicit dependency as it is already defined implicitly."));
  }

  @Test
  void shouldRaiseIssuesInBicep() {
    BicepVerifier.verify("RedundantResourceDependenciesCheck/test.bicep", new RedundantResourceDependenciesCheck());
  }
}
