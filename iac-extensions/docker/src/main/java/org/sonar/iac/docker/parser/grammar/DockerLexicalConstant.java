/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.parser.grammar;

import org.sonar.iac.common.parser.grammar.LexicalConstant;

public class DockerLexicalConstant {


  public static final String COMMENT = "(?:" + LexicalConstant.SINGLE_LINE_COMMENT_HASH + ")";
  public static final String EOL = "(?:\\r\\n|[" + LexicalConstant.LINE_TERMINATOR + "])";
  public static final String LINE_BREAK = "(?:\\\\[" + LexicalConstant.WHITESPACE + "]*+" + EOL + ")";
  public static final String STRING_LITERAL_WITH_QUOTES = "\"(?:[^\"\\\\]*+(?:\\\\[\\s\\S])?+)*+\"";
  public static final String STRING_LITERAL_WITHOUT_QUOTES = "(?:(?!" + LINE_BREAK+ ")[^\\s])++";
  public static final String STRING_LITERAL_WITHOUT_QUOTES_NO_EQUALS = "[^\\s=]++";
  public static final String STRING_LITERAL_OLD = "(?:(?:" + STRING_LITERAL_WITH_QUOTES + ")|(?:" + STRING_LITERAL_WITHOUT_QUOTES + "))+";
  public static final String KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX = "(?:" + STRING_LITERAL_WITH_QUOTES + ")|(?:" + STRING_LITERAL_WITHOUT_QUOTES_NO_EQUALS + ")";
  public static final String STRING_UNTIL_EOL = ".+";
  public static final String EQUALS_OPERATOR = "=";

  /**
   * IDENTIFIERS
   */

  /**
   * LITERAL
   */
  private static final String IDENTIFIER_START = "[a-zA-Z_\\x7f-\\xff]";

  /**
   * '$' sign is allowed in double quoted string and heredoc only when it does not conflict with the
   * encapsulated variable expression, i.e when it not followed with '{' or a starting identifier character.
   */
  private static final String PERMITTED_EMBEDDED_DOLAR = "(?:\\$(?!\\{|" + IDENTIFIER_START + "))";

  private static final String NON_SPECIAL_CHARACTERS = "(?:[^\"\\\\$\\{])";

  // TODO: Handle custom escaping for escaped characters in strings
  private static final String ESCAPED_CHARACTER_OR_STANDALONE_BACKSLASH = "(?:\\\\[\\s\\S]?)";

  public static final String STRING_WITH_ENCAPS_VAR_CHARACTERS = "(?:(?:"
    + NON_SPECIAL_CHARACTERS
    + "|" + PERMITTED_EMBEDDED_DOLAR
    + "|" + ESCAPED_CHARACTER_OR_STANDALONE_BACKSLASH
    + ")++)";

  public static final String QUOTED_STRING_LITERAL = "(?:"
    + "\"" + STRING_WITH_ENCAPS_VAR_CHARACTERS + "?+" + "\""
    + "|'(?:[^'\\\\]*+(?:\\\\[\\s\\S])?+)*+'"
    + ")";


  private DockerLexicalConstant() {
  }
}
