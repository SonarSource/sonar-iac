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
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.typed.Input;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.impl.CompoundTextRange;
import org.sonar.sslr.grammar.GrammarRuleKey;

/**
 * For parsing Heredoc blocks inside a Dockerfile an extended parser with a different grammar root rule is used.
 * This parser takes care of the offset of the initial Heredoc in the Dockerfile itself.
 * It provides an extended NodeBuilder which adjust the Heredoc tokens with the initial offset.
 */
public class DockerHeredocParser extends DockerParser {

  private final DockerHeredocNodeBuilder heredocNodeBuilder;

  private DockerHeredocParser(DockerHeredocNodeBuilder heredocNodeBuilder, GrammarRuleKey rootRule) {
    super(heredocNodeBuilder, rootRule);
    this.heredocNodeBuilder = heredocNodeBuilder;
  }

  public static DockerHeredocParser create() {
    return new DockerHeredocParser(new DockerHeredocNodeBuilder(), DockerLexicalGrammar.HEREDOC_FORM_CONTENT);
  }

  public DockerTree parse(String source, TextRange originalTextRange) {
    this.heredocNodeBuilder.setOriginalTextRange((CompoundTextRange) originalTextRange);
    return super.parse(source);
  }

  static class DockerHeredocNodeBuilder extends DockerNodeBuilder {

    private CompoundTextRange originalTextRange;

    @Override
    protected TextRange tokenRange(Input input, int startIndex, String value) {
      return originalTextRange.computeTextRangeAtIndex(startIndex, value);
    }

    protected void setOriginalTextRange(CompoundTextRange originalTextRange) {
      this.originalTextRange = originalTextRange;
    }
  }
}
