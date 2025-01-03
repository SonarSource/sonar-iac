/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionPrecedenceTest extends TerraformTreeModelTest {

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
  @ParameterizedTest
  @CsvSource({
    "a && b || c,         (a && b) || c",
    "a || b && c,         a || (b && c)",
    "a == b && c,         (a == b) && c",
    "a && b == c,         a && (b == c)",
    "a != b && c,         (a != b) && c",
    "a && b != c,         a && (b != c)",
    "a == b != c,         (a == b) != c",
    "a > b == c,          (a > b) == c",
    "a == b > c,          a == (b > c)",
    "a >= b == c,         (a >= b) == c",
    "a == b >= c,         a == (b >= c)",
    "a < b == c,          (a < b) == c",
    "a == b < c,          a == (b < c)",
    "a <= b == c,         (a <= b) == c",
    "a == b <= c,         a == (b <= c)",
    "a > b >= c < d <= e, (((a > b) >= c) < d) <= e",
    "a + b > c,           (a + b) > c",
    "a > b + c,           a > (b + c)",
    "a - b > c,           (a - b) > c",
    "a > b - c,           a > (b - c)",
    "a + b - c,           (a + b) - c",
    "a * b + c,           (a * b) + c",
    "a + b * c,           a + (b * c)",
    "a / b + c,           (a / b) + c",
    "a + b / c,           a + (b / c)",
    "a % b + c,           (a % b) + c",
    "a + b % c,           a + (b % c)",
    "a * b / c % e,       ((a * b) / c) % e",
    "a && !b,             a && (! b)",
    "!a || b,             (! a) || b",
    "a + -b,              a + (- b)",
    "a + -b - c,          (a + (- b)) - c"
  })
  void binary_operators_precedence(String code, String expected) {
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
      for (Tree child : tree.children()) {
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
