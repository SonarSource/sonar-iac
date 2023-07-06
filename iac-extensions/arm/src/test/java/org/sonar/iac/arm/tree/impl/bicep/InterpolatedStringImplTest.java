package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.extension.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class InterpolatedStringImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.INTERPOLATED_STRING);


  @Test
  void shouldParseSimpleInterpolatedString() {
    String code = code("'abc123DEF'");

    InterpolatedString tree = (InterpolatedString) parser.parse(code, null);
    assertThat(tree.value()).isEqualTo("abc123DEF");
    assertThat(tree.is(ArmTree.Kind.INTERPOLATED_STRING)).isTrue();

    SyntaxToken token1 = (SyntaxToken) tree.children().get(0);
    assertThat(token1.value()).isEqualTo("'");

    SyntaxToken token2 = (SyntaxToken) tree.children().get(1);
    assertThat(token2.children()).isEmpty();
    assertThat(token2.comments()).isEmpty();

    SyntaxToken token3 = (SyntaxToken) tree.children().get(2);
    assertThat(token3.value()).isEqualTo("'");

    assertThat(tree.children()).hasSize(3);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "'123'",
    "'abc'",
    "'A'",
    "'Z'",
    "'a'",
    "'z'",
    "'AAAAA123'",
    "'123zz'",
    "'123aa789'",
    "'123BB789'",
  })
  void shouldParseValidInterpolatedString(String value) {
    String code = code(value);

    InterpolatedString tree = (InterpolatedString) parser.parse(code, null);
    assertThat(tree.value()).isEqualTo(value.replace("'", ""));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    ".12'3456",
    "-",
    "_A1",
    "$123'",
    "{123}",
    "(abc",
  })
  void shouldFailOnInvalidInterpolatedString(String value) {
    String code = code(value);

    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse 'null:1:1'");
  }
}
