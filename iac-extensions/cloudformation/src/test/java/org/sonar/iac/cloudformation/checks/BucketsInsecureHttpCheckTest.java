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
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class BucketsInsecureHttpCheckTest {

  @Test
  void shouldRaiseIssuesInYaml() {
    CloudformationVerifier.verify("BucketsInsecureHttpCheck/BucketsInsecureHttpCheck.yaml", new BucketsInsecureHttpCheck());
  }

  @Test
  void shouldRaiseIssuesInJson() {
    CloudformationVerifier.verify("BucketsInsecureHttpCheck/BucketsInsecureHttpCheck.json", new BucketsInsecureHttpCheck(),
      new Verifier.Issue(range(5, 14, 5, 31)),
      new Verifier.Issue(range(11, 14, 11, 31),
        "Make sure authorizing HTTP requests is safe here.",
        new SecondaryLocation(range(34, 41, 34, 45), "HTTPS requests are denied.")),
      new Verifier.Issue(range(43, 14, 43, 31)),
      new Verifier.Issue(range(77, 14, 77, 31)),
      new Verifier.Issue(range(109, 14, 109, 31)),
      new Verifier.Issue(range(141, 14, 141, 31)));
  }
}
