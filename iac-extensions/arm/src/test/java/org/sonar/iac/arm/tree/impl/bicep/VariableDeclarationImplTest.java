package org.sonar.iac.arm.tree.impl.bicep;

import com.sonar.sslr.api.RecognitionException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;

class VariableDeclarationImplTest {
  BicepParser parser = BicepParser.create(BicepLexicalGrammar.VARIABLE_DECLARATION);

  @ParameterizedTest
  @CsvSource({
    "variable foo = 42",
    "variable foo =42",
    "variable foo=42",
    "variable foo= 42",
    "variable foo = 'abc'",
    "variable foo = true",
  })
  void shouldParseSimpleVariableDeclaration(String code) {
    VariableDeclaration tree = (VariableDeclaration) parser.parse(code);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.name().value()).isEqualTo("foo");
    softly.assertThat(tree.value()).isInstanceOfAny(NumericLiteral.class, StringLiteral.class, BooleanLiteral.class);
    softly.assertThat(tree.children()).hasSize(4);
    softly.assertAll();
  }

  @ParameterizedTest
  @CsvSource({
    /* "variablefoo = 42", */ // TODO: Need to think of a way to enforce space between keyword and identifier
    "var foo = 42",
  })
  void shouldFailOnInvalidVariableDeclaration(String code) {
    Assertions.assertThatThrownBy(() -> parser.parse(code)).isInstanceOf(RecognitionException.class);
  }
}
