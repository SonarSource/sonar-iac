/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.Test;
import org.sonar.iac.common.tree.api.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpressionPrecedenceTest extends TerraformTreeModelTest {

  /**
   * Precedence levels according to specification
   *
   * Level    Operators
   *   6      * / %
   *   5      + -
   *   4      > >= < <=
   *   3      == !=
   *   2      &&
   *   1      ||
   */
  @Test
  public void binary_operators_precedence() throws Exception {
    assertPrecedence("a && b || c", "(a && b) || c");
    assertPrecedence("a || b && c", "a || (b && c)");
    assertPrecedence("a == b && c", "(a == b) && c");
    assertPrecedence("a && b == c", "a && (b == c)");
    assertPrecedence("a != b && c", "(a != b) && c");
    assertPrecedence("a && b != c", "a && (b != c)");
    assertPrecedence("a == b != c", "(a == b) != c");
    assertPrecedence("a > b == c", "(a > b) == c");
    assertPrecedence("a == b > c", "a == (b > c)");
    assertPrecedence("a >= b == c", "(a >= b) == c");
    assertPrecedence("a == b >= c", "a == (b >= c)");
    assertPrecedence("a < b == c", "(a < b) == c");
    assertPrecedence("a == b < c", "a == (b < c)");
    assertPrecedence("a <= b == c", "(a <= b) == c");
    assertPrecedence("a == b <= c", "a == (b <= c)");
    assertPrecedence("a > b >= c < d <= e", "(((a > b) >= c) < d) <= e");
    assertPrecedence("a + b > c", "(a + b) > c");
    assertPrecedence("a > b + c", "a > (b + c)");
    assertPrecedence("a - b > c", "(a - b) > c");
    assertPrecedence("a > b - c", "a > (b - c)");
    assertPrecedence("a + b - c", "(a + b) - c");
    assertPrecedence("a * b + c", "(a * b) + c");
    assertPrecedence("a + b * c", "a + (b * c)");
    assertPrecedence("a / b + c", "(a / b) + c");
    assertPrecedence("a + b / c", "a + (b / c)");
    assertPrecedence("a % b + c", "(a % b) + c");
    assertPrecedence("a + b % c", "a + (b % c)");
    assertPrecedence("a * b / c % e", "((a * b) / c) % e");
  }

  @Test
  public void binary_operators_combined_with_unary_operators() {
    assertPrecedence("a && !b", "a && (! b)");
    assertPrecedence("!a || b", "(! a) || b");
    assertPrecedence("a + -b", "a + (- b)");
    assertPrecedence("a + -b - c", "(a + (- b)) - c");
  }

  private void assertPrecedence(String code, String expected) {
    ExpressionTree expression = parse(code, HclLexicalGrammar.EXPRESSION);
    String actual = dumpWithParentheses(expression).stream().collect(Collectors.joining(" "));
    assertThat(actual).isEqualTo(expected);
  }

  private static List<String> dumpWithParentheses(@Nullable Tree tree) {
    if (tree == null) {
      return Collections.emptyList();
    } else if (tree instanceof SyntaxToken) {
      return Collections.singletonList(((SyntaxToken) tree).value());
    } else {
      List<String> childrenAsString = new ArrayList<>();
      for (Tree child: tree.children()) {
        List<String> childAsString = dumpWithParentheses(child);
        if (childAsString.size() == 1) {
          childrenAsString.add(childAsString.get(0));
        } else if (childAsString.size() > 1) {
          childrenAsString.add("(" + childAsString.stream().collect(Collectors.joining(" ")) + ")");
        }
      }

      return childrenAsString;
    }
  }
}
