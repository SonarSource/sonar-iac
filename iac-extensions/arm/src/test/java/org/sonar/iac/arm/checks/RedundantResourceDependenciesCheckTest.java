/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.testing.Verifier.issue;

class RedundantResourceDependenciesCheckTest {
  @Test
  void shouldRaiseIssuesInJson() {
    // TODO SONARIAC-1426: S6952: Cover `*ResourceId` functions in the `dependsOn` block
    // should also raise on line 11 (`dependsOn` with `resourceId`). On line 13 there is `dependsOn` with more complex function,
    // which we can't resolve, because it's dynamic, so it's a known FN.
    ArmVerifier.verify("RedundantResourceDependenciesCheck/test.json", new RedundantResourceDependenciesCheck(),
      issue(15, 8, 15, 27, "Remove this explicit dependency as it is already defined implicitly.",
        Verifier.secondary(27, 40, 27, 58, "Implicit dependency is created via the \"reference\" function.")));
  }

  @Test
  void shouldRaiseIssuesInBicep() {
    BicepVerifier.verify("RedundantResourceDependenciesCheck/test.bicep", new RedundantResourceDependenciesCheck());
  }
}
