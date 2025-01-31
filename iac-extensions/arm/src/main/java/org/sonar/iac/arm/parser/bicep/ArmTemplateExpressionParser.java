/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.arm.parser.bicep;

import com.sonar.sslr.api.typed.Input;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

/**
 * The ARM Template JSON syntax allows to write function call as StringLiteral in Bicep syntax.
 * To access these function calls, the Bicep paser is utilized to convert the literal into a FunctionCall tree or more general an Expression tree.
 * E.g. "[function_call('foo')]" will be converted to a function call expression.
 */
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

  private static class ArmTemplateExpressionNodeBuilder extends BicepNodeBuilder {
    private TextRange originalTextRange;

    /**
     * The BicepParser is used to convert JSON StringLiteral to FunctionCall expression including arguments to be also converted to nodes.
     * As just the value of the StringLiteral is provided to the parser,
     * the generated token locations need to adapted by the original location in file of the literal which represents the function call.
     */
    @Override
    protected TextRange tokenRange(Input input, int startIndex, String value) {
      var range = super.tokenRange(input, startIndex, value);
      var originalStartLine = originalTextRange.start().line();

      // Shift the offset only for the first line of the parsed expression
      var startLineOffsetShift = 0;
      var endLineOffsetShift = 0;
      if (range.start().line() == 1) {
        var originalOffset = originalTextRange.start().lineOffset();
        // Offset +1 to reduce the text range by the opening bracket '['
        startLineOffsetShift = originalOffset + 1;
        if (range.end().line() == 1) {
          endLineOffsetShift = originalOffset;
        }
      }

      // As the line numbers are 1-based, each line shift needs to reduced by 1
      return TextRanges.range(
        range.start().line() + originalStartLine - 1,
        range.start().lineOffset() + startLineOffsetShift,
        range.end().line() + originalStartLine - 1,
        range.end().lineOffset() + endLineOffsetShift);
    }

    protected void setOriginalTextRange(TextRange originalTextRange) {
      this.originalTextRange = originalTextRange;
    }
  }
}
