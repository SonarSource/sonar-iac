/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
