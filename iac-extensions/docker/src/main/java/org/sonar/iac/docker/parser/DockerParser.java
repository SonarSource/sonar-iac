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
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.parser.grammar.DockerGrammar;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

import static org.sonar.iac.common.extension.ParseException.createGeneralParseException;

public class DockerParser extends ActionParser<DockerTree> implements TreeParser<Tree> {

  private final DockerPreprocessor preprocessor = new DockerPreprocessor();
  private final DockerNodeBuilder nodeBuilder;

  protected DockerParser(DockerNodeBuilder nodeBuilder, GrammarRuleKey rootRule) {
    super(StandardCharsets.UTF_8,
      DockerLexicalGrammar.createGrammarBuilder(),
      DockerGrammar.class,
      new TreeFactory(),
      nodeBuilder,
      rootRule);
    this.nodeBuilder = nodeBuilder;
  }

  public static DockerParser create() {
    return create(DockerLexicalGrammar.FILE);
  }

  public static DockerParser create(GrammarRuleKey rootRule) {
    return new DockerParser(new DockerNodeBuilder(), rootRule);
  }

  @Override
  public DockerTree parse(String source, @Nullable InputFileContext inputFileContext) {
    var preprocessorResult = preprocessor.process(source);
    nodeBuilder.setPreprocessorResult(preprocessorResult);
    try {
      DockerTree tree = super.parse(preprocessorResult.processedSourceCode());
      setParents(tree);
      return tree;
    } catch (RecognitionException e) {
      InputFile inputFile = null;
      if (inputFileContext != null) {
        inputFile = inputFileContext.inputFile;
      }
      throw RecognitionExceptionAdjuster.adjustLineAndColumnNumber(
        e,
        preprocessorResult.processedSourceCode(),
        preprocessorResult.sourceOffset(),
        inputFile);
    }
  }

  @Override
  public DockerTree parse(String source) {
    return parse(source, null);
  }

  private static void setParents(DockerTree tree) {
    for (Tree children : tree.children()) {
      DockerTree child = (DockerTree) children;
      child.setParent(tree);
      setParents(child);
    }
  }

  static final class RecognitionExceptionAdjuster {
    private static final String PARSING_ERROR_MESSAGE = "Parse error at line %d column %d %s";
    private static final Pattern RECOGNITION_EXCEPTION_LINE_COLUMN_PATTERN = Pattern.compile("Parse error at line (?<line>\\d+) column (?<column>\\d+)(?<rest>.*)");

    private RecognitionExceptionAdjuster() {
    }

    public static ParseException adjustLineAndColumnNumber(
      RecognitionException originalException,
      String sourceCode,
      DockerPreprocessor.SourceOffset sourceOffset,
      @Nullable InputFile inputFile) {

      var matcher = RECOGNITION_EXCEPTION_LINE_COLUMN_PATTERN.matcher(originalException.getMessage());
      TextPointer position = null;
      RecognitionException fixedException = originalException;
      if (matcher.find()) {
        var line = Integer.parseInt(matcher.group("line"));
        var column = Integer.parseInt(matcher.group("column"));
        String rest = matcher.group("rest");
        int index = computeIndexFromLineAndColumn(sourceCode, line, column);
        int[] correctedLineAndColumn = sourceOffset.sourceLineAndColumnAt(index);
        if (inputFile != null) {
          position = inputFile.newPointer(correctedLineAndColumn[0], correctedLineAndColumn[1] - 1);
        }
        var newErrorMessage = String.format(PARSING_ERROR_MESSAGE, correctedLineAndColumn[0], correctedLineAndColumn[1], rest);
        fixedException = new RecognitionException(correctedLineAndColumn[0], newErrorMessage, originalException.getCause());
      }

      return createGeneralParseException("parse", inputFile, fixedException, position);
    }

    /**
     * Method computeIndexFromLineAndColumn was heavily inspired from Input class of SSLR library.
     * Method isNewLine was directly copy/pasted from the same library.
     */
    private static int computeIndexFromLineAndColumn(String code, int line, int column) {
      char[] chars = code.toCharArray();
      var currentLine = 1;
      var currentColumn = 0;
      int index = -1;
      while (index + 1 < chars.length && (line != currentLine || column != currentColumn)) {
        index++;
        if (isNewLine(chars, index)) {
          currentLine++;
          currentColumn = 0;
        } else {
          currentColumn++;
        }
      }
      return index;
    }

    private static boolean isNewLine(char[] input, int i) {
      return input[i] == '\n' || (input[i] == '\r' && (i + 1 == input.length || input[i + 1] != '\n'));
    }
  }
}
