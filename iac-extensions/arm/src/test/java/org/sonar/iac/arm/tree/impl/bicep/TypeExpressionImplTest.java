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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.TypeExpression;

import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class TypeExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseTypeExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.TYPE_EXPRESSION)
      // ambient Type Reference
      .matches("array")
      .matches("  array")
      .matches("array[]")
      .matches("array?")
      .matches("array[][]")
      .matches("array[][][]")
      .matches("array[]?")
      .matches("array?")
      .matches("bool")
      .matches("int")
      .matches("array | int")
      .matches("bool | int")
      .matches("bool[] | int?")
      .matches("tuple[1].name")
      .matches("tuple[*]")
      .matches("tuple[*].name")
      .matches("withNested.*.foo")
      .matches("very.long[1].type[]")
      .matches("very.long[1].type[0]")
      .matches("very.long[1].type.*")
      .matches("very.long[1].type.*.foo")
      .matches("bool[][]? | int")
      .matches("bool[][] | int?")

      .notMatches("array[]?[]")
      .notMatches("array??")
      .notMatches("bool[]?[]")
      .notMatches("int??");
  }

  @Test
  void shouldParseSimpleTypeExpression() {
    SingularTypeExpression tree = parse("array", BicepLexicalGrammar.TYPE_EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.SINGULAR_TYPE_EXPRESSION);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("array");
  }

  @Test
  void shouldParseComplexTypeExpression() {
    TypeExpression tree = parse("array | ( abc )? | null ", BicepLexicalGrammar.TYPE_EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.TYPE_EXPRESSION);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("array", "|", "(", "abc", ")", "?", "|", "null");
    List<SingularTypeExpression> expressions = tree.expressions();
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(expressions.get(0)))
      .containsExactly("array");
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(expressions.get(1)))
      .containsExactly("(", "abc", ")", "?");
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(expressions.get(2)))
      .containsExactly("null");
    Assertions.assertThat(expressions).hasSize(3);
  }
}
