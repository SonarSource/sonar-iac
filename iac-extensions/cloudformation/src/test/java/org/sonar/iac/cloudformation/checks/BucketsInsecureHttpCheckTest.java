/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class BucketsInsecureHttpCheckTest {

  @Test
  void shouldRaiseIssuesInYaml() {
    CloudformationVerifier.verify("BucketsInsecureHttpCheck/BucketsInsecureHttpCheck.yaml", new BucketsInsecureHttpCheck());
  }

  @Test
  void shouldRaiseIssuesInJson() {
    // SONARIAC-1803: a statement whose Bool.aws:SecureTransport is "true" is not an HTTPS-only attempt,
    // so the primary issue is raised without per-field secondaries.
    CloudformationVerifier.verify("BucketsInsecureHttpCheck/BucketsInsecureHttpCheck.json", new BucketsInsecureHttpCheck(),
      new Verifier.Issue(range(5, 14, 5, 31)),
      new Verifier.Issue(range(11, 14, 11, 31)),
      new Verifier.Issue(range(43, 14, 43, 31)),
      new Verifier.Issue(range(77, 14, 77, 31)),
      new Verifier.Issue(range(109, 14, 109, 31)),
      new Verifier.Issue(range(141, 14, 141, 31)));
  }
}
