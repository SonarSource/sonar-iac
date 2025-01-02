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
package org.sonar.iac.arm.parser.bicep;

import org.sonar.sslr.grammar.GrammarRuleKey;

public enum BicepKeyword implements GrammarRuleKey {

  EXISTING("existing"),
  RESOURCE("resource"),
  TYPE("type"),
  OUTPUT("output"),
  TARGET_SCOPE("targetScope"),
  FOR("for"),
  IN("in"),
  IF("if"),
  PARAMETER("param"),
  FUNC("func"),
  METADATA("metadata"),
  VARIABLE("var"),
  EXTENSION("extension"),
  IMPORT("import"),
  WITH("with"),
  AS("as"),
  FROM("from"),
  MODULE("module");

  private final String value;

  BicepKeyword(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
