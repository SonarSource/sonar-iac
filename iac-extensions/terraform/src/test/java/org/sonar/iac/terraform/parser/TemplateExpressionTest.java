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
      .notMatches("\"%{ for a,b,c in b}foo\"");
  }
}
