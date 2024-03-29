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
