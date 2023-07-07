package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.parser.utils.Assertions;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;

public class InterpolatedStringImplTest extends BicepTreeModelTest {
  @Test
  void shouldMatchValidStrings() {
    Assertions.assertThat(BicepLexicalGrammar.INTERPOLATED_STRING)
      .matches("'${123}'")
      .matches("'a${123}'")
      .matches("'${123}b'")
      .matches("'a${123}b'")
      .matches("'a${123}b${456}c'")
      .matches("'a${123}${456}c'")
      .notMatches("'abc'")
      .notMatches("123");
  }

  @Test
  void shouldBuildTreeCorrectly() {
    ArmTree tree = createParser(BicepLexicalGrammar.INTERPOLATED_STRING)
      .parse("'a${123}b${456}c'");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(InterpolatedString.class);
    softly.assertThat(tree.children()).hasSize(11);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.INTERPOLATED_STRING);
    softly.assertAll();
  }
}
