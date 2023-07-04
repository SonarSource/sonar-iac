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

  public static final String LITERAL_VALUE = "[a-zA-Z0-9]++";
}
