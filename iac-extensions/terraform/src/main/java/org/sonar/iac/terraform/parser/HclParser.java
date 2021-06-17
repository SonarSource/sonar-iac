/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import com.sonar.sslr.api.typed.ActionParser;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclGrammar;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class HclParser extends ActionParser<TerraformTree> implements TreeParser<Tree> {

  public HclParser() {
    this(HclLexicalGrammar.FILE);
  }

  public HclParser(GrammarRuleKey rootRule) {
    super(
      StandardCharsets.UTF_8,
      HclLexicalGrammar.createGrammarBuilder(),
      HclGrammar.class,
      new TreeFactory(),
      new HclNodeBuilder(),
      rootRule);
  }

  @Override
  public TerraformTree parse(String source, @Nullable InputFileContext inputFileContext) {
    return parse(source);
  }
}
