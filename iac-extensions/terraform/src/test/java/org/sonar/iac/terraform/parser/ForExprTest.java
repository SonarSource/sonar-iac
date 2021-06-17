/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
      .notMatches("{for a,b,c in b: c => d}")
    ;
  }
}
