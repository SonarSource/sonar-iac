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

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
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
    DockerPreprocessor.PreprocessorResult preprocessorResult = preprocessor.process(source);
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

  static class RecognitionExceptionAdjuster {
    private static final String PARSING_ERROR_MESSAGE = "Parse error at line %d column %d %s";
    private static final Pattern RECOGNITION_EXCEPTION_LINE_COLUMN_PATTERN = Pattern.compile("Parse error at line (?<line>\\d+) column (?<column>\\d+)(?<rest>.*)");

    private RecognitionExceptionAdjuster() {
    }

    public static ParseException adjustLineAndColumnNumber(
      RecognitionException originalException,
      String sourceCode,
      DockerPreprocessor.SourceOffset sourceOffset,
      @Nullable InputFile inputFile) {

      Matcher m = RECOGNITION_EXCEPTION_LINE_COLUMN_PATTERN.matcher(originalException.getMessage());
      TextPointer position = null;
      RecognitionException fixedException = originalException;
      if (m.find()) {
        int line = Integer.parseInt(m.group("line"));
        int column = Integer.parseInt(m.group("column"));
        String rest = m.group("rest");
        int index = computeIndexFromLineAndColumn(sourceCode, line, column);
        int[] correctedLineAndColumn = sourceOffset.sourceLineAndColumnAt(index);
        if (inputFile != null) {
          position = inputFile.newPointer(correctedLineAndColumn[0], correctedLineAndColumn[1] - 1);
        }
        String newErrorMessage = String.format(PARSING_ERROR_MESSAGE, correctedLineAndColumn[0], correctedLineAndColumn[1], rest);
        fixedException = new RecognitionException(correctedLineAndColumn[0], newErrorMessage, originalException.getCause());
      }

      return ParseException.throwParseException("parse", inputFile, fixedException, position);
    }

    /**
     * Method computeIndexFromLineAndColumn was heavily inspired from Input class of SSLR library.
     * Method isNewLine was directly copy/pasted from the same library.
     */
    private static int computeIndexFromLineAndColumn(String code, int line, int column) {
      char[] chars = code.toCharArray();
      int currentLine = 1;
      int currentColumn = 0;
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
