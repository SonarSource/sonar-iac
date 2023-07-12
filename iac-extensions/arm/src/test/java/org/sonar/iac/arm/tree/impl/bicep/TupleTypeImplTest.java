/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import static org.sonar.iac.common.testing.IacTestUtils.code;

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

      .notMatches("[typeExpr]")
      .notMatches("[ typeExpr]")
      .notMatches("[typeExpr ]")
      .notMatches("[ typeExpr ]")
      .notMatches("[\ntypeExpr\ntypeExpr2]")
      .notMatches("[]typeExpr")
      .notMatches("[\ntypeExpr")
      .notMatches("typeExpr]")
      .notMatches("{typeExpr}");
  }

  @Test
  void shouldParseSimpleTupleType() {
    String code = code("[\n@functionName123() typeExpr\n]");
    TupleType tree = parse(code, BicepLexicalGrammar.TUPLE_TYPE);
    assertThat(tree.is(ArmTree.Kind.TUPLE_TYPE)).isTrue();

    TupleItem tupleItem = tree.items().get(0);

    assertThat(tupleItem.decorators())
      .map(ArmTestUtils::recursiveTransformationOfTreeChildrenToStrings)
      .containsExactly(List.of("@", "functionName123", "(", ")"));
    assertThat(tupleItem.typeExpression().value()).isEqualTo("typeExpr");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("[");
    assertThat(tree.children().get(1)).isInstanceOf(TupleItem.class);
    assertThat(((SyntaxToken) tree.children().get(2)).value()).isEqualTo("]");
    assertThat(tree.children()).hasSize(3);
  }
}
