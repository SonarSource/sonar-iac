/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class ObjectElementTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.OBJECT_ELEMENT)
      .matches("a : 1")
      .matches("a = 1")
      .matches("{} : 1")
      .matches("a : {}")
      .matches("a.b = 1")
      .notMatches("a :")
      .notMatches("a =");
  }
}
