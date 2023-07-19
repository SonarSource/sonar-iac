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
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TupleItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class TupleItemImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseTupleItem() {
    ArmAssertions.assertThat(BicepLexicalGrammar.TUPLE_ITEM)
      .matches("typeExpr")
      .matches("array[][]")
      .matches("array | int")
      .matches("bool[]?[] | int??")
      .matches("@functionName123() typeExpr")
      .matches("@description('some desc')\n@maxLength(12)\ntypeExpr")

      .notMatches("typeExpr-")
      .notMatches("typeExpr {}");
  }

  @Test
  void shouldParseSimpleTupleItem() {
    String code = code("@functionName123() typeExpr");
    TupleItem tree = parse(code, BicepLexicalGrammar.TUPLE_ITEM);
    assertThat(tree.is(ArmTree.Kind.TUPLE_ITEM)).isTrue();

    assertThat(tree.decorators())
      .map(ArmTestUtils::recursiveTransformationOfTreeChildrenToStrings)
      .containsExactly(List.of("@", "functionName123", "(", ")"));
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.typeExpression()))
      .containsExactly("typeExpr");
    assertThat(tree.children().get(0)).isInstanceOf(Decorator.class);
    assertThat(tree.children().get(1)).isInstanceOf(SingularTypeExpression.class);
    assertThat(tree.children()).hasSize(2);
  }
}
