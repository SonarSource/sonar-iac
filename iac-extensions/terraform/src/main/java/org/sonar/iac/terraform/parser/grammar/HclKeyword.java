/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser.grammar;

import org.sonar.sslr.grammar.GrammarRuleKey;

public enum HclKeyword implements GrammarRuleKey {
  FOR("for"),
  IF("if"),
  ELSE("else"),
  END_IF("endif"),
  END_FOR("endfor"),
  IN("in");

  private final String value;

  HclKeyword(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
