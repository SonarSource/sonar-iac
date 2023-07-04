package org.sonar.iac.arm.parser;

import com.sonar.sslr.api.typed.ActionParser;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.sonar.iac.arm.parser.bicep.BicepGrammar;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.parser.bicep.BicepNodeBuilder;
import org.sonar.iac.arm.parser.bicep.TreeFactory;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class BicepParser extends ActionParser<ArmTree> implements TreeParser<Tree> {

  private final BicepNodeBuilder nodeBuilder;
  @Nullable
  private InputFileContext inputFileContext;

  protected BicepParser(BicepNodeBuilder nodeBuilder, GrammarRuleKey rootRule) {
    super(StandardCharsets.UTF_8,
      BicepLexicalGrammar.createGrammarBuilder(),
      BicepGrammar.class,
      new TreeFactory(),
      nodeBuilder,
      rootRule);
    this.nodeBuilder = nodeBuilder;
  }

  public static BicepParser create() {
    return create(BicepLexicalGrammar.FILE);
  }

  public static BicepParser create(GrammarRuleKey rootRule) {
    try {
      return new BicepParser(new BicepNodeBuilder(), rootRule);
    } catch (RuntimeException exception) {
      if (exception.getCause() instanceof InvocationTargetException) {
        throw new RuntimeException("Please make sure that all methods in your grammar are public", exception);
      } else {
        throw exception;
      }
    }
  }

  @Override
  public ArmTree parse(String source, @Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
    return super.parse(source);
  }

}
