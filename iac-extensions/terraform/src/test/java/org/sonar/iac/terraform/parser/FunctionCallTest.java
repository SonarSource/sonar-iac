/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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

class FunctionCallTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.FUNCTION_CALL)
      .matches("a()")
      .matches("a(1, 2)")
      .matches("a(1, 2,)")
      .matches("a(1, 2...)")
      .matches("a(b(1))")
      .notMatches("a(1")
      .notMatches("a(,)")
      .notMatches("a(...)")
      .notMatches("a(1 2)");
  }
}
