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
package org.sonar.iac.terraform.checks.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.tree.impl.TerraformTreeModelTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.sonar.iac.terraform.checks.utils.TerraformUtils.getResourceName;

class TerraformUtilsTest extends TerraformTreeModelTest {

  @ParameterizedTest
  @CsvSource(textBlock = """
    a, null
    a.b, b
    a.b.c, c
    a[1], null
    a.b[1], b
    a.b.c[1], c
    a[1].b, b
    a[1].b.c, c
    a[1].b[2].c[3], c
    """, nullValues = "null")
  void shouldGetResourceName(String expression, String expectedName) {
    var tree = (ExpressionTree) parse(expression, HclLexicalGrammar.EXPRESSION);

    var name = getResourceName(tree);

    if (expectedName == null) {
      assertThat(name).isEmpty();
    } else {
      assertThat(name).hasValue(expectedName);
    }
  }
}
