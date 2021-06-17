/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser.grammar;

import org.sonar.sslr.grammar.GrammarRuleKey;

public enum HclPunctuator implements GrammarRuleKey {
  COLON(":"),
  COMMA(","),
  DOUBLEARROW("=>"),
  DOT("."),
  EQU("="),
  ELLIPSIS("..."),
  LBRACKET("["),
  RBRACKET("]"),
  LCURLYBRACE("{"),
  RCURLYBRACE("}"),
  LPARENTHESIS("("),
  RPARENTHESIS(")"),
  OR("||"),
  AND("&&"),
  EQUAL("=="),
  NOT_EQUAL("!="),
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
  TILDE_RCURLY("~}")
  ;

  private final String value;

  HclPunctuator(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
