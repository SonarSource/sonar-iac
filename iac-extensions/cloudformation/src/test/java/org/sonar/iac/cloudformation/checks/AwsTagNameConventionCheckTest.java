/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class AwsTagNameConventionCheckTest {

  @Test
  void testYamlWithDefaultPattern() {
    CloudformationVerifier.verify("AwsTagNameConventionCheck/default.yaml", new AwsTagNameConventionCheck());
  }

  @Test
  void testYamlWithCustomPattern() {
    AwsTagNameConventionCheck check = new AwsTagNameConventionCheck();
    check.format = "^([a-z-]*[a-z]:)*([a-z-]*[a-z])$";
    CloudformationVerifier.verify("AwsTagNameConventionCheck/custom.yaml", check);
  }

  @Test
  void testJsonWithDefaultPattern() {
    CloudformationVerifier.verify("AwsTagNameConventionCheck/default.json", new AwsTagNameConventionCheck(),
      new Verifier.Issue(range(10, 19, 10, 43),
        "Rename tag key \"anycompany:cost-center\" to match the regular expression \"^(([^:]++:)*+([A-Z][A-Za-z]*+))$\"."));
  }
}
