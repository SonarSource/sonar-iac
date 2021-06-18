/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser.grammar;

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
  private static final String SINGLE_LINE_COMMENT_CONTENT = "(?:[^\\n\\r])*+";
  private static final String SINGLE_LINE_COMMENT1 = "//" + SINGLE_LINE_COMMENT_CONTENT;
  private static final String SINGLE_LINE_COMMENT2 = "#" + SINGLE_LINE_COMMENT_CONTENT;
  private static final String MULTI_LINE_COMMENT = "/\\*[\\s\\S]*?\\*/";
  public static final String COMMENT = "(?:" + SINGLE_LINE_COMMENT1 + "|" + SINGLE_LINE_COMMENT2 + "|" + MULTI_LINE_COMMENT + ")";

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
