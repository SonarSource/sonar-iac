/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class AwsTagNameConventionCheckTest {

  @Test
  void test_default_yaml() {
    CloudformationVerifier.verify("AwsTagNameConventionCheck/default.yaml", new AwsTagNameConventionCheck());
  }

  @Test
  void test_custom() {
    AwsTagNameConventionCheck check = new AwsTagNameConventionCheck();
    check.format = "^([a-z-]*[a-z]:)*([a-z-]*[a-z])$";
    CloudformationVerifier.verify("AwsTagNameConventionCheck/custom.yaml", check);
  }

  @Test
  void test_default_json() {
    CloudformationVerifier.verify("AwsTagNameConventionCheck/default.json", new AwsTagNameConventionCheck(),
      new Verifier.Issue(range(10, 19, 10, 43),
        "Rename tag key \"anycompany:cost-center\" to match the regular expression \"^([A-Z][A-Za-z]*:)*([A-Z][A-Za-z]*)$\"."),
      new Verifier.Issue(range(14, 19, 14, 47)));
  }
}
