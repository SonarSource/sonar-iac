/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class TemplateExpressionTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.QUOTED_TEMPLATE)
      .matches("\"foo\"")
      .matches("\"foo${a}\"")
      .matches("\"foo${\"bar\"}\"")
      .matches("\"${\"bar\"}foo\"")
      .matches("\"foo${\"bar\"}foo\"")
      .matches("\"${\"bar\"}\"")
      .matches("\"${\"bar${\"bar\"}\"}\"")
      .matches("\"foo${~ a}\"")
      .matches("\"foo${a ~}\"")
      .matches("\"%{ if a != 1 }foo%{ endif }\"")
      .matches("\"%{ if a != 1 }foo%{ else }bar%{ endif }\"")
      .matches("\"%{ if a != 1 }${ \"foo\" }%{ endif }\"")
      .matches("\"%%{ if a != 1 }foo\"")
      .matches("\"%{ for a in b}foo%{ endfor }\"")
      .matches("\"%{ for a,b in b}foo%{ endfor }\"")
      .notMatches("\"foo$${\"bar\"}\"")
      .notMatches("\"foo${ ~ a}\"")
      .notMatches("\"%{ if a != 1 }foo%\"")
      .notMatches("\"%{ for a,b,c in b}foo%{ endfor }\"")
      .notMatches("\"%{ for a,b,c in b}foo\"")
    ;
  }
}
