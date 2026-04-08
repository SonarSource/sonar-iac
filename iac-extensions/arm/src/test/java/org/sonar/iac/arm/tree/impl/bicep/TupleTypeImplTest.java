/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TupleItem;
import org.sonar.iac.arm.tree.api.bicep.TupleType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class TupleTypeImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseTupleType() {
    ArmAssertions.assertThat(BicepLexicalGrammar.TUPLE_TYPE)
      .matches("[]")
      .matches("[ ]")
      .matches("[\n]")
      .matches("[\ntypeExpr\n]")
      .matches("[\ntypeExpr\ntypeN\n]")
      .matches("[\n@functionName123() typeExpr\n]")
      .matches("[@description('some desc')\n@maxLength(12)\ntypeExpr\n]")
      .matches("[\n@description('some desc')\n@maxLength(12)\ntypeExpr\n]")
      .matches("[typeExpr]")
      .matches("[typeExpr typeN]")
      .matches("[ typeExpr]")
      .matches("[typeExpr ]")
      .matches("[ typeExpr ]")
      .matches("[\ntypeExpr\ntypeExpr2]")
      // comma-separated tuple types
      .matches("[typeExpr, typeN]")
      .matches("[typeExpr, typeN, typeM]")
      .matches("[ typeExpr, typeN ]")
      .matches("[typeExpr,typeN]")
      .matches("[@decorator() typeExpr, typeN]")
      .matches("[@decorator(), typeExpr, typeN]")
      .matches("[string, int, bool]")
      .matches("[string, int, bool,]")

      .notMatches("[]typeExpr")
      .notMatches("[\ntypeExpr")
      .notMatches("typeExpr]")
      .notMatches("{typeExpr}");
  }

  @Test
  void shouldParseSimpleTupleType() {
    String code = """
      [
      @functionName123() typeExpr
      ]""";
    TupleType tree = parse(code, BicepLexicalGrammar.TUPLE_TYPE);
    assertThat(tree.is(ArmTree.Kind.TUPLE_TYPE)).isTrue();

    TupleItem tupleItem = tree.items().get(0);

    assertThat(tupleItem.decorators())
      .map(ArmTestUtils::recursiveTransformationOfTreeChildrenToStrings)
      .containsExactly(List.of("@", "functionName123", "(", ")"));
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tupleItem.typeExpression()))
      .containsExactly("typeExpr");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("[");
    assertThat(tree.children().get(1)).isInstanceOf(TupleItem.class);
    assertThat(((SyntaxToken) tree.children().get(2)).value()).isEqualTo("]");
    assertThat(tree.children()).hasSize(3);
  }

  @Test
  void shouldParseCommaSeparatedTupleType() {
    String code = "[ @discriminator('type'), typeA | typeB | { type: 'c', value: object }, string]";
    TupleType tree = parse(code, BicepLexicalGrammar.TUPLE_TYPE);
    assertThat(tree.is(ArmTree.Kind.TUPLE_TYPE)).isTrue();

    assertThat(tree.items()).hasSize(2);

    TupleItem firstItem = tree.items().get(0);
    assertThat(firstItem.decorators())
      .map(ArmTestUtils::recursiveTransformationOfTreeChildrenToStrings)
      .containsExactly(List.of("@", "discriminator", "(", "type", ")"));
    assertThat(recursiveTransformationOfTreeChildrenToStrings(firstItem.typeExpression()))
      .containsExactly("typeA", "|", "typeB", "|", "{", "type", ":", "c", ",", "value", ":", "object", "}");

    TupleItem secondItem = tree.items().get(1);
    assertThat(secondItem.decorators()).isEmpty();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(secondItem.typeExpression()))
      .containsExactly("string");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("[");
    assertThat(tree.children().get(1)).isInstanceOf(TupleItem.class);
    assertThat(((SyntaxToken) tree.children().get(2)).value()).isEqualTo(",");
    assertThat(tree.children().get(3)).isInstanceOf(TupleItem.class);
    assertThat(((SyntaxToken) tree.children().get(4)).value()).isEqualTo("]");
    assertThat(tree.children()).hasSize(5);
  }
}
