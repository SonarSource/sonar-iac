/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SpreadProperty;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class SpreadPropertyImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseSpreadProperty() {
    ArmAssertions.assertThat(BicepLexicalGrammar.SPREAD_PROPERTY)
      .matches("...identifier123")
      .matches("... identifier123")
      .matches("...{key:'val'}")

      .notMatches("key:value")
      .notMatches(".identifier123")
      .notMatches("..identifier123")
      .notMatches("...")
      .notMatches("identifier123...")
      .notMatches("...identifier123...")
      .notMatches("....identifier123");
  }

  @Test
  void shouldParseObjectSpreadProperty() {
    String code = "...identifier123";

    SpreadProperty spreadProperty = parse(code, BicepLexicalGrammar.OBJECT_PROPERTY);
    assertThat(spreadProperty.is(ArmTree.Kind.SPREAD_PROPERTY)).isTrue();

    SyntaxToken spreadOperator = (SyntaxToken) spreadProperty.children().get(0);
    assertThat(spreadOperator.value()).isEqualTo("...");

    Expression iterable = (Expression) spreadProperty.children().get(1);
    assertThat(iterable).isEqualTo(spreadProperty.iterable());
    assertThat(iterable.is(ArmTree.Kind.VARIABLE)).isTrue();
    ArmAssertions.assertThat(iterable).asWrappedIdentifier().hasValue("identifier123");

    assertThat(spreadProperty.children()).hasSize(2);
  }

  @Test
  void shouldConvertToString() {
    SpreadProperty spreadProperty = parse("...identifier123", BicepLexicalGrammar.OBJECT_PROPERTY);
    assertThat(spreadProperty).hasToString("...identifier123");
  }
}
