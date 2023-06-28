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

import org.junit.jupiter.api.Test;

import static org.sonar.iac.common.api.checks.SecondaryLocation.secondary;
import static org.sonar.iac.common.testing.Verifier.issue;

class SubscriptionOwnerCapabilitiesCheckTest {
  @Test
  void check() {
    ArmVerifier.verify("SubscriptionOwnerCapabilitiesCheck/Microsoft.Authorization_roleDefinitions/test.json",
      new SubscriptionOwnerCapabilitiesCheck(),
      issue(6, 14, 6, 55, "Narrow the number of actions or the assignable scope of this custom role.",
        secondary(12, 24, 12, 27, "Allows all actions."),
        secondary(17, 10, 17, 31, "High scope level.")));
  }
}
