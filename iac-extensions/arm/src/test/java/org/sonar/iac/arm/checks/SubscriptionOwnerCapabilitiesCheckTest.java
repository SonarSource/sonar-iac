/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class SubscriptionOwnerCapabilitiesCheckTest {
  @ParameterizedTest(name = "[{index}] Should raise an issue for scope \"{0}\"")
  @ValueSource(strings = {
    "[subscription().id]",
    "[managementGroup().id]",
    "/subscriptions/b24988ac-6180-42a0-ab88-20f7382dd24c",
    "/providers/Microsoft.Management/managementGroups/b24988ac-6180-42a0-ab88-20f7382dd24c"
  })
  void shouldDetectSensitiveScopesJson(String assignableScope) {
    String content = readTemplateAndReplace("SubscriptionOwnerCapabilitiesCheck/Authorization_roleDefinitions.json", assignableScope);

    int contentLength = assignableScope.length();
    ArmVerifier.verifyContent(content,
      new SubscriptionOwnerCapabilitiesCheck(),
      issue(38, 14, 38, 55, "Narrow the number of actions or the assignable scope of this custom role.",
        new SecondaryLocation(range(44, 24, 44, 27), "Allows all actions"),
        new SecondaryLocation(range(49, 10, 49, 12 + contentLength), "High scope level")));
  }

  @Test
  void shouldDetectSensitiveScopesBicep() {
    BicepVerifier.verify("SubscriptionOwnerCapabilitiesCheck/Authorization_roleDefinitions.bicep", new SubscriptionOwnerCapabilitiesCheck());
  }
}
