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
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class ObjectTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.OBJECT)
      .matches("{ }")
      .matches("{ a : 1 }")
      .matches("{ a: 1, b: 2 }")
      .matches("{ a: 1, b: 2, }")
      .matches("{ a: 1, b = 2 }")
      .matches("{ a: 1, b = { c: 3 } }")
      .matches("{ a: 1\n b = 3 }")
      .notMatches("{ a: 1 b = 3 }")
      .notMatches("")
      .notMatches("{");
  }
}
