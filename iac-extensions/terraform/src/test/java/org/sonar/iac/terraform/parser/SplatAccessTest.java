/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

class SplatAccessTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.EXPRESSION)
      .matches("a.*")
      .matches("a.b.*")
      .matches("a.*.b")
      .matches("a.b.*.c.d")
      .matches("a[*]")
      .matches("a[*].b")
      .matches("a[*].*.b")
      .matches("a.*[*]") // Not valid HCL, but our parser allows it.
      .matches("(a).*")
      .notMatches("*.b")
      .notMatches("a[*]b")
      .notMatches("a.[*]");
  }
}
