package org.sonar.iac.arm.parser.bicep;

import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.api.typed.Input;
import com.sonar.sslr.api.typed.NodeBuilder;
import java.util.Collections;
import java.util.List;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.arm.tree.impl.bicep.SyntaxTokenImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class BicepNodeBuilder implements NodeBuilder {

  public static final char BYTE_ORDER_MARK = '\uFEFF';


  @Override
  public Object createNonTerminal(GrammarRuleKey ruleKey, Rule rule, List<Object> children, int startIndex, int endIndex) {
    for (Object child : children) {
      if (child instanceof SyntaxToken) {
        return child;
      }
    }

    return new AbstractArmTreeImpl() {
      @Override
      public List<Tree> children() {
        throw new UnsupportedOperationException();
      }

      @Override
      public ArmTree.Kind getKind() {
        return ArmTree.Kind.TOKEN;
      }
    };
  }

  @Override
  public Object createTerminal(Input input, int startIndex, int endIndex, List<Trivia> trivias, TokenType type) {
    String value = input.substring(startIndex, endIndex);
    TextRange range = tokenRange(input, startIndex, value);
    return new SyntaxTokenImpl(value, range, Collections.emptyList());
  }

  private static TextRange tokenRange(Input input, int startIndex, String value) {
    int[] lineAndColumn = input.lineAndColumnAt(startIndex);
    char[] fileChars = input.input();
    boolean hasByteOrderMark = fileChars.length > 0 && fileChars[0] == BYTE_ORDER_MARK;
    int column = applyByteOrderMark(lineAndColumn[1], hasByteOrderMark) - 1;
    return TextRanges.range(lineAndColumn[0], column, value);
  }

  private static int applyByteOrderMark(int column, boolean hasByteOrderMark) {
    return hasByteOrderMark ? (column - 1) : column;
  }
}
