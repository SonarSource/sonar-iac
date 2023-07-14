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
package org.sonar.iac.arm.tree.impl.bicep.expression;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.bicep.expression.TernaryExpression;
import org.sonar.iac.arm.tree.impl.bicep.BicepTreeModelTest;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class TernaryExpressionImplTest extends BicepTreeModelTest {

  @Test
  void parseTernaryExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.EXPRESSION)
      .matches("true ? 2 : 3")
      .matches("true?2:3")
      .matches("5 > 3 ? 5 : 3")
      .matches("5 > 3 ? 3 < 2 ? 1 : 2 : 0")

      .notMatches("1 ? 2 :")
      .notMatches("? 2 :3")
      .notMatches("1 ? :");
  }

  @Test
  void parseSimplyTernaryExpression() {
    TernaryExpression expression = parse("true ? 2 : 3", BicepLexicalGrammar.EXPRESSION);
    assertThat(expression.getKind()).isEqualTo(ArmTree.Kind.TERNARY_EXPRESSION);

    assertThat(expression.condition()).asBooleanLiteral().isTrue();
    assertThat(expression.ifTrueExpression()).asNumericLiteral().hasValue(2);
    assertThat(expression.elseExpression()).asNumericLiteral().hasValue(3);

    List<String> children = expression.children().stream()
      .map(t -> {
        if (t instanceof TextTree) {
          return ((TextTree) t).value();
        } else if (t instanceof BooleanLiteral) {
          return ((BooleanLiteral) t).value() ? "true" : "false";
        } else {
          throw new RuntimeException("Invalid cast from " + t.getClass());
        }
      })
      .collect(Collectors.toList());
    assertThat(children).containsExactly("true", "?", "2", ":", "3");
  }

}
