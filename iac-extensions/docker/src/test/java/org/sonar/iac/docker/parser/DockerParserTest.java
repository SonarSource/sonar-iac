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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.docker.tree.api.DockerTree.Kind.EXEC_FORM;
import static org.sonar.iac.docker.tree.api.DockerTree.Kind.SHELL_FORM;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class DockerParserTest {

  @Test
  void raiseParsingErrorWithoutComments() {
    var code = "ONBUILD unknown";
    ParseException exception = assertThrows(ParseException.class, () -> parse(code, DockerLexicalGrammar.INSTRUCTION));
    assertThat(exception.getMessage()).isEqualTo("Cannot parse 'null'");
    assertThat(exception.getDetails()).startsWith("Parse error at line 1 column 9");
  }

  @Test
  void raiseParsingErrorLeadingComments() {
    var code = """
      # comment which will be removed
      ONBUILD unknown""";
    ParseException exception = assertThrows(ParseException.class, () -> parse(code, DockerLexicalGrammar.INSTRUCTION));
    assertThat(exception.getMessage()).isEqualTo("Cannot parse 'null'");
    assertThat(exception.getDetails()).startsWith("Parse error at line 2 column 9");
  }

  @Test
  void raiseParsingErrorMultilineInstruction() {
    var code = """
      ONBUILD\s
      unknown""";
    ParseException exception = assertThrows(ParseException.class, () -> parse(code, DockerLexicalGrammar.INSTRUCTION));
    assertThat(exception.getMessage()).isEqualTo("Cannot parse 'null'");
    assertThat(exception.getDetails()).startsWith("Parse error at line 2 column 1");
  }

  @Test
  void raiseParsingErrorMultilineInstructionWithComment() {
    var code = """
      ONBUILD\s
      # comment which will be removed
      unknown""";
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

  @Test
  void shouldParseFileWithOnlyComments() {
    var code = """
      # empty file
      """;

    assertThatNoException().isThrownBy(() -> parse(code, DockerLexicalGrammar.FILE));
  }

  @Test
  void shouldParseWithStrangeQuotes() {
    var code = """
      FROM ubuntu:20.04
      ENTRYPOINT [ "/bin/bash”, “-c” ]
      CMD ["source script"]
      """;

    var file = (File) parse(code, DockerLexicalGrammar.FILE);

    assertThat(file.body().dockerImages()).hasSize(1);
    var dockerImage = file.body().dockerImages().get(0);
    var entrypoint = (EntrypointInstruction) dockerImage.instructions().get(0);
    assertThat(entrypoint.getKindOfArgumentList()).isEqualTo(SHELL_FORM);
    var cmd = (CmdInstruction) dockerImage.instructions().get(1);
    assertThat(cmd.getKindOfArgumentList()).isEqualTo(EXEC_FORM);
  }
}
