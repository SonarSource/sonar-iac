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
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.typed.Input;
import org.sonar.iac.common.api.tree.TextTree;
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
    return new DockerHeredocParser(new DockerHeredocNodeBuilder(), DockerLexicalGrammar.HEREDOC_CONTENT);
  }

  public DockerTree parse(TextTree token) {
    this.heredocNodeBuilder.setOriginalTextRange((CompoundTextRange) token.textRange());
    return super.parse(token.value());
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
