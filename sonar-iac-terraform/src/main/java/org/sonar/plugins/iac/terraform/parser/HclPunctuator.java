/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.iac.terraform.parser;

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
