/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.common.parser.grammar;

public class LexicalConstant {

  private LexicalConstant() {
  }

  /**
   * LF, CR, LS, PS
   */
  public static final String LINE_TERMINATOR = "\\n\\r\\u2028\\u2029";

  /**
   * Tab, Vertical Tab, Form Feed, Space, No-break space, Byte Order Mark, Any other Unicode "space separator"
   */
  public static final String WHITESPACE = "\\t\\u000B\\f\\u0020\\u00A0\\uFEFF\\p{Zs}";

  /**
   * Comment
   */
  public static final String SINGLE_LINE_COMMENT_CONTENT = "(?:[^\\n\\r])*+";
  public static final String SINGLE_LINE_COMMENT_DOUBLE_SLASH = "//" + SINGLE_LINE_COMMENT_CONTENT;
  public static final String SINGLE_LINE_COMMENT_HASH = "#" + SINGLE_LINE_COMMENT_CONTENT;
  public static final String MULTI_LINE_COMMENT = "/\\*[\\s\\S]*?\\*/";

  /**
   * IDENTIFIERS
   */
  public static final String IDENTIFIER_START = "[a-zA-Z]";
  public static final String IDENTIFIER_PART = "[a-zA-Z0-9\\-\\_]";
  public static final String IDENTIFIER = IDENTIFIER_START + IDENTIFIER_PART + "*+";

  /**
   * Heredoc
   */
  public static final String HEREDOC_LITERAL = "<<-?(" + IDENTIFIER + ")[" + LINE_TERMINATOR + "][\\s\\S]*?" + "[" + LINE_TERMINATOR + "]" + "[" + WHITESPACE + "]*+\\1(?!\\S)";

  /**
   * String
   */
  public static final String STRING_LITERAL = "\"(?:[^\"\\\\]*+(?:\\\\[\\s\\S])?+)*+\"";
  public static final String QUOTED_TEMPLATE_STRING_CHARACTERS = "(?:[^\"\\\\$%]|\\\\[\\s\\S]?|\\$\\$\\{|\\$(?!\\{)|%%\\{|%(?!\\{))++";
  public static final String STRING_WITHOUT_INTERPOLATION = "\"(?:" + QUOTED_TEMPLATE_STRING_CHARACTERS + ")?+\"";
  public static final String TEMPLATE_LITERAL = "(?:[^$%\"]|\\$(?!\\{)|%(?!\\{))++";

  /**
   * Numeric Literal
   */
  public static final String NUMERIC_LITERAL = "[0-9]+(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?";

  public static final String NUMERIC_INDEX = "[0-9]+";
}
