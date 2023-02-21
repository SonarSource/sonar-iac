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

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.api.tree.Comment;

import static org.assertj.core.api.Assertions.assertThat;

class DockerPreprocessorTest {

  final DockerPreprocessor preprocessor = new DockerPreprocessor();

  enum ESCAPE_CHAR {
    DEFAULT, ALTERNATIVE
  }

  @ParameterizedTest
  @CsvSource({
    "'foo\\\nbar'",
    "'foo\\\r\nbar'",
    "'foo\\\u2028bar'",
    "'foo\\\u2029bar'",
    "'foo\\\rbar'",
    "'foo\\    \nbar'",
    "'\\\nfoobar'",
    "'foobar\\\n'"
  })
  void processSingleEscapedLinebreak(String input) {
    String output = preprocessor.process(input).processedSourceCode();
    assertThat(output).isEqualTo("foobar");
  }

  @Test
  void processAlternativeEscapedLinebreak() {
    String output = preprocessor.process("# escape=`\nfoo`\nbar").processedSourceCode();
    assertThat(output).isEqualTo("foobar");
  }

  @Test
  void processNoEscapedLinebreak() {
    String input = "foo\nbar";
    String output = preprocessor.process(input).processedSourceCode();
    assertThat(output).isEqualTo(input);
  }

  @Test
  void processNoLinebreak() {
    String input = "foo bar";
    String output = preprocessor.process(input).processedSourceCode();
    assertThat(output).isEqualTo(input);
  }

  @Test
  void processMultipleEscapedLinebreaks() {
    String input = "foo\\\nbar\\\npong";
    String output = preprocessor.process(input).processedSourceCode();
    assertThat(output).isEqualTo("foobarpong");
  }

  @ParameterizedTest
  @CsvSource({
    "'RUN test\\\n# my comment\npong',                                              3, 1",
    "'RUN test\\\n     \npong',                                                     3, 1",
    "'RUN test\\\n# simple comment\n     \n\n  # comment with spaces before\npong', 6, 1"
  })
  void processRunCommandWithInlineCommentAndEmptyLines(String input, int line, int column) {
    DockerPreprocessor.PreprocessorResult result = preprocessor.process(input);
    assertThat(result.processedSourceCode()).isEqualTo("RUN testpong");
    DockerPreprocessor.SourceOffset sourceOffset = result.sourceOffset();
    assertThat(sourceOffset.sourceLineAndColumnAt(7)).isEqualTo(new int[] {1, 8});
    assertThat(sourceOffset.sourceLineAndColumnAt(8)).isEqualTo(new int[] {line, column});
  }

  @Test
  void getInlineComment() {
    DockerPreprocessor.PreprocessorResult result = preprocessor.process("RUN test\\\n# my comment\npong");
    assertThat(result.processedSourceCode()).isEqualTo("RUN testpong");
    Map<Integer, Comment> commentMap = result.commentMap();
    assertThat(commentMap).hasSize(1);
    assertThat(commentMap.keySet()).containsExactly(2);
    assertThat(commentMap.get(2).value()).isEqualTo("# my comment");
  }

  @Test
  void sourceLineAndColumnWithOneEscapedLinebreak() {
    DockerPreprocessor.SourceOffset sourceOffset = preprocessor.process("foo\\\nbar").sourceOffset();
    assertThat(sourceOffset.sourceLineAndColumnAt(2)).isEqualTo(new int[] {1,3});
    assertThat(sourceOffset.sourceLineAndColumnAt(3)).isEqualTo(new int[] {2,1});
  }

  @ParameterizedTest
  @CsvSource({
    "'',                      DEFAULT",
    "'# escape=`',            ALTERNATIVE",
    "'# escape=\\\\',         DEFAULT",
    "' # escape=`',           ALTERNATIVE",
    "'FROM foo\n# escape=`',  DEFAULT",
    "'# comment\n# escape=`', ALTERNATIVE",
    "'#escape=`',             ALTERNATIVE",
    "'# escape =`',           ALTERNATIVE",
    "'# escape= `',           ALTERNATIVE",
    "'# escape=',             DEFAULT",
    "'# escape = `',          ALTERNATIVE",
  })
  void determineEscapeCharacter(String source, ESCAPE_CHAR expectedEscapeCharacter) {
    String escapeChar = ESCAPE_CHAR.DEFAULT == expectedEscapeCharacter ? DockerPreprocessor.DEFAULT_ESCAPE_CHAR : DockerPreprocessor.ALTERNATIVE_ESCAPE_CHAR;
    assertThat(DockerPreprocessor.determineEscapeCharacter(source)).isEqualTo(escapeChar);
  }
}
