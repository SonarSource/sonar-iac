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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.UnaryOperator;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class UnaryOperatorImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseUnaryOperator() {
    ArmAssertions.assertThat(BicepLexicalGrammar.UNARY_OPERATOR)
      .matches("!")
      .matches("-")
      .matches("+")

      .notMatches("!!")
      .notMatches("--")
      .notMatches("-+")
      .notMatches("++")
      .notMatches("foo")
      .notMatches("123")
      .notMatches("a!")
      .notMatches("-5")
      .notMatches("+8")
      .notMatches("-!-");
  }

  @Test
  void shouldParseSimpleUnaryOperator() {
    String code = code("-");
    UnaryOperator tree = parse(code, BicepLexicalGrammar.UNARY_OPERATOR);
    assertThat(tree.is(ArmTree.Kind.UNARY_OPERATOR)).isTrue();
    assertThat(tree.value()).isEqualTo("-");
    assertThat(tree.children()).map(token -> ((TextTree) token).value()).containsExactly("-");
    ArmAssertions.assertThat(tree.textRange()).hasRange(1, 0, 1, 1);
  }
}
