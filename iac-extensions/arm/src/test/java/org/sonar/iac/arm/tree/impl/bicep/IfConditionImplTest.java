/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Variable;
import org.sonar.iac.arm.tree.api.bicep.IfCondition;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class IfConditionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseIfCondition() {
    ArmAssertions.assertThat(BicepLexicalGrammar.IF_CONDITION)
      .matches("if (condition){key:value}")
      .matches("if(condition){key:value}")
      .matches("if(condition){}")
      .matches("if ( condition ) { key : value }")

      .notMatches("if{}")
      .notMatches("if(condition)")
      .notMatches("if{key:value}");
  }

  @Test
  void shouldParseIfConditionWithDetailedAssertions() {
    String code = code("if(condition){key:value}");

    IfCondition tree = parse(code, BicepLexicalGrammar.IF_CONDITION);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.IF_CONDITION)).isTrue();

    softly.assertThat(((ArmTree) tree.children().get(1)).is(ArmTree.Kind.PARENTHESIZED_EXPRESSION)).isTrue();
    softly.assertThat(tree.condition().is(ArmTree.Kind.VARIABLE)).isTrue();
    softly.assertThat(((Variable) tree.condition()).identifier())
      .extracting(i -> ((Identifier) i).value())
      .isEqualTo("condition");

    softly.assertThat(tree.object().is(ArmTree.Kind.OBJECT_EXPRESSION)).isTrue();

    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly(
      "if", "(", "condition", ")", "{", "key", ":", "value", "}");

    softly.assertAll();
  }

}
