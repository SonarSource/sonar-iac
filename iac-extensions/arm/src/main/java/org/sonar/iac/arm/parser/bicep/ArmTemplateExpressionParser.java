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

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.Input;
import java.io.IOException;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.tree.impl.LocationImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

import static org.sonar.iac.common.extension.ParseException.createParseException;

public final class ArmTemplateExpressionParser extends BicepParser {
  private final ArmTemplateExpressionNodeBuilder nodeBuilder;

  private ArmTemplateExpressionParser(ArmTemplateExpressionNodeBuilder nodeBuilder, GrammarRuleKey rootRule) {
    super(nodeBuilder, rootRule);
    this.nodeBuilder = nodeBuilder;
  }

  public static ArmTemplateExpressionParser create() {
    return new ArmTemplateExpressionParser(new ArmTemplateExpressionNodeBuilder(), BicepLexicalGrammar.BINARY_EXPRESSION);
  }

  public ArmTree parse(ScalarTree scalar, InputFileContext inputFileContext) {
    var scalarTextRange = scalar.metadata().textRange();
    // Remove enclosing double quotes.
    var expressionTextRange = TextRanges.range(
      scalarTextRange.start().line(), scalarTextRange.start().lineOffset() + 1,
      scalarTextRange.end().line(), scalarTextRange.end().lineOffset() - 1);

    // In case when scalar is a multiline string, snakeyaml returns it without line breaks and indentation.
    // This breaks text ranges of child trees, so we have to read the source code to have correct TextRanges.
    String content;
    try {
      content = inputFileContext.inputFile.contents();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    var location = LocationImpl.fromTextRange(expressionTextRange, content);
    // Remove enclosing square brackets.
    var locationNoSquareBrackets = location.shift(1, -2);
    var expressionString = content.substring(
      locationNoSquareBrackets.position(),
      locationNoSquareBrackets.position() + locationNoSquareBrackets.length());
    var noSquareBracketsTextRange = locationNoSquareBrackets.toTextRange(content);
    nodeBuilder.setStartTextRange(noSquareBracketsTextRange);
    nodeBuilder.setContent(content);
    try {
      return super.parse(expressionString);
    } catch (RecognitionException e) {
      throw createParseException(
        "Failed to parse ARM template expression: " + scalar.value(),
        inputFileContext,
        new BasicTextPointer(scalar.metadata().textRange()));
    }
  }

  static class ArmTemplateExpressionNodeBuilder extends BicepNodeBuilder {
    private TextRange startTextRange;
    private String content;

    @Override
    protected TextRange tokenRange(Input input, int startIndex, String value) {
      var textRange = super.tokenRange(input, startIndex, value);
      var startLocation = LocationImpl.fromTextRange(startTextRange, content);
      var tokenLocation = LocationImpl.fromTextRange(textRange, new String(input.input()));
      var shiftedLocation = new LocationImpl(startLocation.position() + tokenLocation.position(), tokenLocation.length());
      return shiftedLocation.toTextRange(content);
    }

    public void setStartTextRange(TextRange startPointer) {
      this.startTextRange = startPointer;
    }

    public void setContent(String content) {
      this.content = content;
    }
  }
}
