/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.TupleItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class TupleItemImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseTupleItem() {
    ArmAssertions.assertThat(BicepLexicalGrammar.TUPLE_ITEM)
      .matches("typeExpr")
      .matches("array[][]")
      .matches("array | int")
      .matches("bool[][]? | int")
      .matches("bool[][] | int?")
      .matches("@functionName123() typeExpr")
      .matches("@description('some desc')\n@maxLength(12)\ntypeExpr")

      .notMatches("typeExpr-")
      .notMatches("typeExpr {}")
      .notMatches("bool[]?[] | int")
      .notMatches("bool[][] | int??");
  }

  @Test
  void shouldParseSimpleTupleItem() {
    var code = "@functionName123() typeExpr";
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
