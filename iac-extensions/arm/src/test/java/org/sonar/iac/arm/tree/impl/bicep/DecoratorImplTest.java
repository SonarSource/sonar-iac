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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.Decorator;

import static org.sonar.iac.common.testing.IacTestUtils.code;

class DecoratorImplTest extends BicepTreeModelTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.DECORATOR);

  @Test
  void shouldParseDecorator() {
    ArmAssertions.assertThat(BicepLexicalGrammar.DECORATOR)
      .matches("@functionName123()")
      .matches("@functionName123(expr, expr)")

      .notMatches("functionName123()")
      .notMatches("@");
  }

  @Test
  void shouldParseDecoratorWithDetailedAssertions() {
    String code = code("@functionName123()");

    Decorator tree = (Decorator) parser.parse(code, null);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.DECORATOR)).isTrue();
    softly.assertThat(tree.functionCall().is(ArmTree.Kind.FUNCTION_CALL)).isTrue();
    softly.assertThat(tree.children()).hasSize(2);

    softly.assertAll();
  }

}
