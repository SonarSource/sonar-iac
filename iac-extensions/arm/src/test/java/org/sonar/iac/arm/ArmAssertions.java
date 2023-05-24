package org.sonar.iac.arm;

import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.testing.TextRangeAssert;

public class ArmAssertions {
  public static TextRangeAssert assertThat(@Nullable TextRange actual) {
    return TextRangeAssert.assertThat(actual);
  }
}
