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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class DockerParserTest {

  @Test
  void raiseParsingErrorWithoutComments() {
    String code = code("ONBUILD unknown");
    ParseException exception = assertThrows(ParseException.class, () -> parse(code, DockerLexicalGrammar.INSTRUCTION));
    assertThat(exception.getMessage()).isEqualTo("Cannot parse 'null'");
    assertThat(exception.getDetails()).startsWith("Parse error at line 1 column 9");
  }

  @Test
  void raiseParsingErrorLeadingComments() {
    String code = code("# comment which will be removed",
      "ONBUILD unknown");
    ParseException exception = assertThrows(ParseException.class, () -> parse(code, DockerLexicalGrammar.INSTRUCTION));
    assertThat(exception.getMessage()).isEqualTo("Cannot parse 'null'");
    assertThat(exception.getDetails()).startsWith("Parse error at line 2 column 9");
  }

  @Test
  void raiseParsingErrorMultilineInstruction() {
    String code = code("ONBUILD ",
      "unknown");
    ParseException exception = assertThrows(ParseException.class, () -> parse(code, DockerLexicalGrammar.INSTRUCTION));
    assertThat(exception.getMessage()).isEqualTo("Cannot parse 'null'");
    assertThat(exception.getDetails()).startsWith("Parse error at line 2 column 1");
  }

  @Test
  void raiseParsingErrorMultilineInstructionWithComment() {
    String code = code("ONBUILD ",
      "# comment which will be removed",
      "unknown");
    InputFile inputFile = mock(InputFile.class);
    Mockito.when(inputFile.toString()).thenReturn("filename.abc");
    InputFileContext inputFileContext = new InputFileContext(mock(SensorContext.class), inputFile);
    ParseException exception = assertThrows(ParseException.class, () -> parse(code, DockerLexicalGrammar.INSTRUCTION, inputFileContext));
    assertThat(exception.getMessage()).isEqualTo("Cannot parse 'filename.abc'");
    assertThat(exception.getDetails()).startsWith("Parse error at line 3 column 1");
  }

  @Test
  void shouldCreateNewExceptionOnInvalidMessageFormat() {
    RecognitionException exception = new RecognitionException(0, "InvalidMessage");
    Throwable throwable = DockerParser.RecognitionExceptionAdjuster.adjustLineAndColumnNumber(exception, null, null, null);
    assertThat(throwable)
      .isInstanceOf(ParseException.class)
      .extracting(exp -> ((ParseException) exp).getDetails())
      .isEqualTo("InvalidMessage");
  }
}
