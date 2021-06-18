/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class AttributeAccessTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.EXPRESSION)
      .matches("a.b")
      .matches("a.b.c")
      .matches("{}.a")
      .matches("\"foo\".a")
      .matches("123.a")
      .matches("a[1].b")
      .matches("(a).b")
      .matches("a.0") // In the spec this is a legacyIndex access. We do parse it as a an attribute access though.
      .matches("a.0.0.b") // Not allowed in the official parser.
      .matches("(a).b")
      .notMatches("a.");
  }
}
