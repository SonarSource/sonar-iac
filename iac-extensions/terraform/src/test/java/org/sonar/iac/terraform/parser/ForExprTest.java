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
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class ForExprTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.EXPRESSION)
      .matches("[for a in b : c]")
      .matches("[for a in b : c if d]")
      .matches("[for a,b in c : d if e]")
      .matches("{for a in b: c => d}")
      .matches("{for a,b in b: c => d}")
      .matches("{for a,b in b: c => d if e}")
      .matches("{for a in b: c => d...}")
      .notMatches("[for a, in b : c]")
      .notMatches("[for a,b,c in b : c]")
      .notMatches("{for a, in b: c => d}")
      .notMatches("{for a,b,c in b: c => d}");
  }
}
