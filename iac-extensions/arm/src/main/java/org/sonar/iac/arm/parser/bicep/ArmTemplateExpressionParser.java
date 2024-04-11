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
package org.sonar.iac.arm.parser.bicep;

import com.sonar.sslr.api.typed.Input;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public final class ArmTemplateExpressionParser extends BicepParser {
  private final ArmTemplateExpressionNodeBuilder nodeBuilder;

  private ArmTemplateExpressionParser(ArmTemplateExpressionNodeBuilder nodeBuilder, GrammarRuleKey rootRule) {
    super(nodeBuilder, rootRule);
    this.nodeBuilder = nodeBuilder;
  }

  public static ArmTemplateExpressionParser create() {
    return new ArmTemplateExpressionParser(new ArmTemplateExpressionNodeBuilder(), BicepLexicalGrammar.BINARY_EXPRESSION);
  }

  public ArmTree parse(ScalarTree scalar) {
    // Remove enclosing square brackets.
    var expressionString = scalar.value().substring(1, scalar.value().length() - 1);
    var scalarTextRange = scalar.metadata().textRange();
    // Expression text range is taken from the data provided by snakeyaml. It will be inaccurate w.r.t the original file in case of
    // multiline strings (allowed in ARM JSON) and strings with escaped line break symbols. This is still in sync with the main AST,
    // however may cause issues in issue highlighting and syntax highlighting.
    var expressionTextRange = TextRanges.range(
      scalarTextRange.start().line(), scalarTextRange.start().lineOffset() + 1,
      scalarTextRange.end().line(), scalarTextRange.end().lineOffset() - 1);

    nodeBuilder.setOriginalTextRange(expressionTextRange);
    return super.parse(expressionString);
  }

  static class ArmTemplateExpressionNodeBuilder extends BicepNodeBuilder {
    private TextRange originalTextRange;

    @Override
    protected TextRange tokenRange(Input input, int startIndex, String value) {
      return computeTextRangeAtIndex(originalTextRange, startIndex, value);
    }

    private static TextRange computeTextRangeAtIndex(TextRange originalTextRange, int startIndex, String value) {
      return TextRanges.range(
        originalTextRange.start().line(), originalTextRange.start().lineOffset() + startIndex, value);
    }

    protected void setOriginalTextRange(TextRange originalTextRange) {
      this.originalTextRange = originalTextRange;
    }

  }
}
