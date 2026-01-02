/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class AnonymousAccessPolicyCheckTest {

  @Test
  void shouldRaiseIssuesInYaml() {
    CloudformationVerifier.verify("AnonymousAccessPolicyCheck/test.yaml", new AnonymousAccessPolicyCheck());
  }

  @Test
  void shouldRaiseIssuesInJson() {
    String message = "Make sure granting public access is safe here.";
    String secondaryMessage = "Related effect.";
    CloudformationVerifier.verify("AnonymousAccessPolicyCheck/test.json", new AnonymousAccessPolicyCheck(),
      new Verifier.Issue(range(39, 23, 39, 26), message,
        new SecondaryLocation(range(37, 24, 37, 31), secondaryMessage)),
      new Verifier.Issue(range(57, 18, 57, 21), message,
        new SecondaryLocation(range(54, 24, 54, 31), secondaryMessage)),
      new Verifier.Issue(range(109, 16, 109, 19), message,
        new SecondaryLocation(range(107, 24, 107, 31), secondaryMessage)));
  }

}
