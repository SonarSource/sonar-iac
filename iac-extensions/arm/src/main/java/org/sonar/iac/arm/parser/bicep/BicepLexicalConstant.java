/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import org.sonar.iac.common.parser.grammar.LexicalConstant;

import static org.sonar.iac.common.parser.grammar.LexicalConstant.MULTI_LINE_COMMENT;
import static org.sonar.iac.common.parser.grammar.LexicalConstant.SINGLE_LINE_COMMENT_DOUBLE_SLASH;
import static org.sonar.iac.common.parser.grammar.LexicalConstant.SINGLE_LINE_COMMENT_HASH;

public class BicepLexicalConstant {

  public static final String EOL = "(?:\\r\\n|[" + LexicalConstant.LINE_TERMINATOR + "])";

  public static final String COMMENT = "(?:"
    + SINGLE_LINE_COMMENT_DOUBLE_SLASH
    + "|" + SINGLE_LINE_COMMENT_HASH
    + "|" + MULTI_LINE_COMMENT + ")";

  public static final String NUMBER = "[0-9]++";
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  public static final String NULL = "null";
  public static final String SINGLE_QUOTED_STRING_CONTENT = "(?:(?!\\$\\{)(?:\\\\[\\S]|[^']))*+";
  public static final String IDENTIFIER_LITERAL = "[a-zA-Z_][a-zA-Z_0-9]*+";
  public static final String AMBIENT_TYPE = "(?:array|bool|int|object|string)(?=\\s|\\)|,|\\[|\\.|\\z)";
  public static final String UNARY_OPERATOR = "!|-|\\+";
  public static final String MULTILINE_STRING = "(?:(?!''')(?:[\\s\\S])?+)*+";
  public static final String EXCLAMATION_SIGN_ALONE = "!(?![=~])";

  private BicepLexicalConstant() {
  }
}
