/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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

import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.api.typed.Input;
import com.sonar.sslr.api.typed.NodeBuilder;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.terraform.tree.impl.TerraformTreeImpl;
import org.sonar.sslr.grammar.GrammarRuleKey;

import static org.sonar.iac.common.parser.NodeBuilderUtils.createComments;
import static org.sonar.iac.common.parser.NodeBuilderUtils.tokenRange;

public class HclNodeBuilder implements NodeBuilder {

  @Override
  public Object createNonTerminal(GrammarRuleKey ruleKey, Rule rule, List<Object> children, int startIndex, int endIndex) {
    for (Object child : children) {
      if (child instanceof SyntaxTokenImpl) {
        return child;
      }
    }

    return new TerraformTreeImpl() {
      @Override
      public List<Tree> children() {
        throw new UnsupportedOperationException();
      }

      @Override
      public TerraformTree.Kind getKind() {
        return TerraformTree.Kind.TOKEN;
      }
    };
  }

  @Override
  public Object createTerminal(Input input, int startIndex, int endIndex, List<Trivia> trivias, TokenType type) {
    String value = input.substring(startIndex, endIndex);
    TextRange range = tokenRange(input, startIndex, value);
    return new SyntaxTokenImpl(value, range, createComments(trivias));
  }
}
