/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.checks.utils;

import com.sonar.sslr.api.typed.Input;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.docker.parser.DockerNodeBuilder;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public final class ResolvedCommandPartParser extends DockerParser {
  private final ResolvedCommandPartNodeBuilder resolvedCommandPartNodeBuilder;

  private ResolvedCommandPartParser(ResolvedCommandPartNodeBuilder resolvedCommandPartNodeBuilder, GrammarRuleKey rootRule) {
    super(resolvedCommandPartNodeBuilder, rootRule);
    this.resolvedCommandPartNodeBuilder = resolvedCommandPartNodeBuilder;
  }

  public static ResolvedCommandPartParser create() {
    return new ResolvedCommandPartParser(new ResolvedCommandPartNodeBuilder(), DockerLexicalGrammar.SHELL_FORM_GENERIC);
  }

  public DockerTree parseWithTextRange(TextRange originalTextRange, String text) {
    this.resolvedCommandPartNodeBuilder.setOriginalTextRange(originalTextRange);
    return super.parse(text);
  }

  static class ResolvedCommandPartNodeBuilder extends DockerNodeBuilder {

    private TextRange originalTextRange;

    @Override
    protected TextRange tokenRange(Input input, int startIndex, String value) {
      return originalTextRange;
    }

    protected void setOriginalTextRange(TextRange originalTextRange) {
      this.originalTextRange = originalTextRange;
    }
  }
}
