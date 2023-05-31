package org.sonar.iac.arm.tree.impl.json;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.VariableDeclaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.VARIABLE_DECLARATION;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class VariableDeclarationImplTest {

  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseVariables() {
    String code = code("{",
      "  \"variables\": {",
      "    \"stringVar\": \"val\",",
      "    \"arrayVar\": [\"val\"],",
      "    \"objectVar\": {\"key\":\"val\"},",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(3);
    assertThat(tree.statements().get(0).is(VARIABLE_DECLARATION)).isTrue();
    assertThat(tree.statements().get(1).is(VARIABLE_DECLARATION)).isTrue();
    assertThat(tree.statements().get(2).is(VARIABLE_DECLARATION)).isTrue();

    VariableDeclaration stringVar = (VariableDeclaration) tree.statements().get(0);
    VariableDeclaration arrayVar = (VariableDeclaration) tree.statements().get(1);
    VariableDeclaration objectVar = (VariableDeclaration) tree.statements().get(2);

    assertThat(stringVar.name()).is(ArmTree.Kind.IDENTIFIER).has("value", "stringVar").hasRange(3,4, 3, 15);
    assertThat(stringVar.value()).isExpression().hasValue("val").hasRange(3,17, 3, 22);
    assertThat(arrayVar.name()).is(ArmTree.Kind.IDENTIFIER).has("value", "arrayVar").hasRange(4,4, 4, 14);
    assertThat(arrayVar.value()).isArrayExpression().hasRange(4,16, 4, 23);
    assertThat(objectVar.name()).is(ArmTree.Kind.IDENTIFIER).has("value", "objectVar").hasRange(5,4, 5, 15);
    assertThat(objectVar.value()).isObjectExpression().hasRange(5,18, 5, 29);
  }

  @Test
  void shouldParseNoVariables() {
    String code = code("{",
      "  \"variables\": {",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
  }
}
