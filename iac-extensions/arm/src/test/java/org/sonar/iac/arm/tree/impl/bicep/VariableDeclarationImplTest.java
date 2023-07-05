package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.bicep.VariableDeclaration;

class VariableDeclarationImplTest {
  BicepParser parser = BicepParser.create(BicepLexicalGrammar.VARIABLE_DECLARATION);
  @ParameterizedTest
  @CsvSource({
    "variable foo = 42",
  })
  void shouldParseSimpleVariableDeclaration(String code) {
    VariableDeclaration tree = (VariableDeclaration) parser.parse(code);
    Assertions.assertThat(tree.name()).isEqualTo("foo");
  }
}
