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
package org.sonar.iac.common.parser.grammar;

import org.sonar.sslr.grammar.GrammarRuleKey;

public enum Punctuator implements GrammarRuleKey {
  COLON(":"),
  DOUBLE_COLON("::"),
  COMMA(","),
  DOUBLEARROW("=>"),
  DOT("."),
  EQU("="),
  ELLIPSIS("..."),
  LBRACKET("["),
  RBRACKET("]"),
  BRACKET("[]"),
  LCURLYBRACE("{"),
  RCURLYBRACE("}"),
  LPARENTHESIS("("),
  RPARENTHESIS(")"),
  PIPE("|"),
  OR("||"),
  AND("&&"),
  COALESCE("??"),
  EQUAL("=="),
  NOT_EQUAL("!="),
  EQUAL_CASE_INSENSITIVE("=~"),
  NOT_EQUAL_CASE_INSENSITIVE("!~"),
  GREATER_THAN(">"),
  GREATER_OR_EQUAL(">="),
  LESS_THAN("<"),
  LESS_OR_EQUAL("<="),
  PLUS("+"),
  MINUS("-"),
  DIV("/"),
  PERCENT("%"),
  STAR("*"),
  QUERY("?"),
  EXCLAMATION("!"),
  DOUBLE_QUOTE("\""),
  DOLLAR_LCURLY("${"),
  DOLLAR_LCURLY_TILDE("${~"),
  PERCENT_LCURLY("%{"),
  PERCENT_LCURLY_TILDE("%{~"),
  TILDE_RCURLY("~}"),
  DOLLAR("$"),
  APOSTROPHE("'"),
  TRIPLE_APOSTROPHE("'''"),
  AT("@");

  private final String value;

  Punctuator(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
