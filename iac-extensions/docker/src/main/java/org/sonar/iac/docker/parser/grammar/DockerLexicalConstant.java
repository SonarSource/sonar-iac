/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

public final class DockerLexicalConstant {

  public static final String COMMENT = "(?:" + LexicalConstant.SINGLE_LINE_COMMENT_HASH + ")";
  public static final String EOL = "(?:\\r\\n|[" + LexicalConstant.LINE_TERMINATOR + "])";
  public static final String LINE_BREAK = "(?:\\\\[" + LexicalConstant.WHITESPACE + "]*+" + EOL + ")";
  public static final String STRING_LITERAL_WITH_QUOTES = "\"(?:[^\"\\\\]*+(?:\\\\[\\s\\S])?+)*+\"";
  public static final String STRING_LITERAL_WITHOUT_QUOTES = "(?:(?!" + LINE_BREAK + ")[^\\s])++";
  public static final String STRING_LITERAL_OLD = "(?:(?:" + STRING_LITERAL_WITH_QUOTES + ")|(?:" + STRING_LITERAL_WITHOUT_QUOTES + "))+";
  public static final String EQUALS_OPERATOR = "=";
  public static final String RBRACKET_END_EXEC_FORM = "\\](?=[" + LexicalConstant.WHITESPACE + "]*+(?:[\r\n]|$))";

  // ** IDENTIFIERS **
  private static final String VAR_IDENTIFIER_START = "[a-zA-Z_0-9]";
  public static final String VAR_IDENTIFIER = VAR_IDENTIFIER_START + "++";
  public static final String ENCAPS_VAR_MODIFIER_SEPARATOR = ":(-|\\+)";
  public static final String ENCAPS_VAR_MODIFIER_GENERIC = "(\\\\}|[^}])+";
  public static final String FLAG_NAME = "[a-z][-a-z]*+";

  // ** LITERAL **
  /**
   * '$' sign is allowed in double quoted string and heredoc only when it does not conflict with the
   * encapsulated variable expression, i.e when it not followed with '{' or a starting identifier character.
   */
  private static final String PERMITTED_EMBEDDED_DOLAR = "(?:\\$(?!\\{|" + VAR_IDENTIFIER_START + "))";

  private static final String NON_SPECIAL_CHARACTERS = "(?:[^\"\\\\$\r\n]|(?<!$)\\{)";

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

  public static final String ESCAPED_UNQUOTED_STRING_CHARACTERS = "\\\\$|\\\\'|\\\\\"";

  public static final String UNQUOTED_STRING_LITERAL = "(?:"
    + "(?:" + ESCAPED_UNQUOTED_STRING_CHARACTERS + "|[^\\s'\"\\$]|\\$(?![a-zA-Z_0-9{]))++"
    + ")";

  /**
   * Allows to match any character sequence as a string. Used as a fallback matcher for shell form.
   */
  public static final String ANY_CHAR_STRING_LITERAL = "[^\n\r]++";

  /**
   * Allow to match the modifier of a variable in a dockerfile. Currently more permissive as it allow to match any characters except unescaped $, ' and "
   * until it find a }. There is also a lookahead in the end to ensure the match is followed by a closing } without matching it.
   */
  public static final String UNQUOTED_VARIABLE_MODIFIER = "(?:"
    + "(?:" + ESCAPED_UNQUOTED_STRING_CHARACTERS + "|[^\\s'\"$}])++(?<!})"
    + ")";

  public static final String UNQUOTED_KEY_LITERAL = "(?:"
    + "(?:" + ESCAPED_UNQUOTED_STRING_CHARACTERS + "|[^\\s'\"$=])++"
    + ")";

  /**
   * Regexes to match Docker heredoc expression as described in the <a href="https://docs.docker.com/engine/reference/builder/#here-documents">Docker reference</a>.
   * It starts with the heredoc indicator (<<), followed by an optional minus, then the 2nd capturing group (the heredoc block name).
   * It can be encapsulated by single or double-quote (we use the 1st capturing group to match the quote), or not.
   * The key part is the reference to the heredoc block name which end the heredoc block by having this element alone in a line.
   * This regex also allow having multiple heredoc block name, with optional other commands between them on the same line;
   * it will then end until the last matched block in 2nd group.
   * Implementation note: `\2` matches the last match of the 2nd capturing group, i.e. the name of the last opening heredoc marker.
   */
  // Match a single heredoc name. E.g: <<KEY
  public static final String HEREDOC_NAME = "<<-?(\"|')?([a-zA-Z0-9_]++)\\1?";
  // Match the whole group of heredoc names. E.g: <<KEY1 something <<KEY2
  private static final String HEREDOC_NAMES = "(?:" + HEREDOC_NAME + "[^<\\r\\n]*)*+";
  // Match the end of the heredoc, which is a line with the last matched heredoc name, followed by EOL or EOF (not matched). E.g: \nKEY1\n
  private static final String HEREDOC_END = EOL + "\\2(?=" + EOL + "|$)";
  // The whole heredoc expression.
  public static final String HEREDOC_EXPRESSION = HEREDOC_NAMES + "[\\s\\S]*?" + HEREDOC_END;

  public static final String IMAGE_ALIAS = "[-a-zA-Z0-9_\\.]+";

  private DockerLexicalConstant() {
  }
}
