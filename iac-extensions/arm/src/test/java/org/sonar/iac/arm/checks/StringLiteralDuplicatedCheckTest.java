package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class StringLiteralDuplicatedCheckTest {
  private static final StringLiteralDuplicatedCheck CHECK = new StringLiteralDuplicatedCheck();

  @Test
  void testEmptyOrNullValueJson() {
    ArmVerifier.verify("StringLiteralDuplicatedCheck/stringLiteralDuplicatedCheck.json", CHECK,
      issue(8, 14, 8, 31, "Define a constant instead of duplicating this literal \"appSuperStorage\" 3 times.",
        new SecondaryLocation(range(10, 23, 10, 40), "Duplication."),
        new SecondaryLocation(range(11, 21, 11, 38), "Duplication.")),
      issue(17, 14, 17, 31, "Define a constant instead of duplicating this literal \"test_duplicated\" 3 times.",
        new SecondaryLocation(range(19, 23, 19, 40), "Duplication."),
        new SecondaryLocation(range(20, 21, 20, 38), "Duplication."))
    );
  }

  @Test
  void testEmptyOrNullValueBicep() {
    BicepVerifier.verify("StringLiteralDuplicatedCheck/stringLiteralDuplicatedCheck.bicep", CHECK);
  }
}
