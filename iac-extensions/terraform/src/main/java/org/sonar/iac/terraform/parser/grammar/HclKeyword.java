/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.terraform.parser.grammar;

import org.sonar.sslr.grammar.GrammarRuleKey;

public enum HclKeyword implements GrammarRuleKey {
  FOR("for"),
  IF("if"),
  ELSE("else"),
  END_IF("endif"),
  END_FOR("endfor"),
  IN("in"),
  DYNAMIC("dynamic");

  private final String value;

  HclKeyword(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
