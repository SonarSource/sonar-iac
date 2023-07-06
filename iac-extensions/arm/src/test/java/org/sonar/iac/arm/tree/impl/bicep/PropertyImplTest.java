package org.sonar.iac.arm.tree.impl.bicep;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class PropertyImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.PROPERTY);

  @Test
  void shouldParseSimpleProperty() {
    String code = code("key:value");

    Property tree = (Property) parser.parse(code, null);
    assertThat(((StringLiteral) tree.value()).value()).isEqualTo("value");
    assertThat(tree.is(ArmTree.Kind.PROPERTY)).isTrue();

    Identifier key = (Identifier) tree.children().get(0);
    assertThat(key.value()).isEqualTo("key");

    SyntaxToken colon = (SyntaxToken) tree.children().get(1);
    assertThat(colon.children()).isEmpty();
    assertThat(colon.comments()).isEmpty();

    StringLiteral value = (StringLiteral) tree.children().get(2);
    assertThat(value.value()).isEqualTo("value");

    assertThat(tree.children()).hasSize(3);
  }

  static Stream<Arguments> shouldParseValidProperty() {
    return Stream.of(
      Arguments.of("key", ":", "value"),
      Arguments.of("key", ": ", "value"),
      Arguments.of("key", " :", "value"),
      Arguments.of("key", " : ", "value"),
      Arguments.of("key1", ": ", "value1"),
      Arguments.of("1key", ": ", "1value"),
      Arguments.of("Ke1", ": ", "VALu3")
    );
  }

  @MethodSource
  @ParameterizedTest(name = "should Parse property: `{0}{1}{2}`")
  void shouldParseValidProperty(String key, String colon, String value) {
    String code = code(key + colon + value);

    Property tree = (Property) parser.parse(code, null);
    assertThat(tree.key().value()).isEqualTo(key);
    assertThat(((StringLiteral) tree.value()).value()).isEqualTo(value);
  }
}
