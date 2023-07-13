package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.bicep.LambdaExpression;

class LambdaExpressionImplTest extends BicepTreeModelTest {
  @Test
  void shouldMatchValidExpressions() {
    ArmAssertions.assertThat(BicepLexicalGrammar.LAMBDA_EXPRESSION)
      .matches("foo => 0")
      .matches("foo => 'a'")
      .matches("(foo) => 'a'")
      .matches("() => 'a'")
      .matches("(foo, bar) => 'a'");
  }

  @Test
  void shouldParseValidExpression() {
    LambdaExpression tree = (LambdaExpression) createParser(BicepLexicalGrammar.LAMBDA_EXPRESSION).parse(
      "(foo, bar) => 0");

    tree.getKind();
    Assertions.assertThat(tree).isInstanceOf(LambdaExpression.class);
    Assertions.assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("(", "foo", "bar", ",", ")", "=>", "0");
  }
}
