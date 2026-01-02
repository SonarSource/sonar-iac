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
package org.sonar.iac.common.extension;

import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.extension.ParseException.createGeneralParseException;
import static org.sonar.iac.common.extension.ParseException.createParseException;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;

class ParseExceptionTest {

  private final RuntimeException cause = new RuntimeException("cause");
  private final TextPointer position = new BasicTextPointer(1, 2);
  private InputFile inputFile;
  private InputFileContext inputFileContext;

  @BeforeEach
  public void init() {
    inputFileContext = createInputFileContextMock("TestFile.abc");
    inputFile = inputFileContext.inputFile;
  }

  @Test
  void shouldCreateGeneralException() {
    ParseException actual = createGeneralParseException("action", inputFile, cause, position);

    assertThat(actual)
      .hasMessage("Cannot action 'dir1/dir2/TestFile.abc:1:3'")
      .extracting(ParseException::getPosition)
      .isEqualTo(position);
    assertThat(actual)
      .extracting(ParseException::getDetails)
      .isEqualTo("cause");
  }

  @Test
  void shouldCreateExceptionNullInputFile() {
    ParseException actual = createGeneralParseException("action", null, cause, position);

    assertThat(actual)
      .hasMessage("Cannot action 'null:1:3'")
      .extracting(ParseException::getPosition)
      .isEqualTo(position);
    assertThat(actual)
      .extracting(ParseException::getDetails)
      .isEqualTo("cause");
  }

  @Test
  void shouldCreateNullPosition() {
    ParseException actual = createGeneralParseException("action", inputFile, cause, null);

    assertThat(actual)
      .hasMessage("Cannot action 'dir1/dir2/TestFile.abc'")
      .extracting(ParseException::getPosition)
      .isNull();
    assertThat(actual)
      .extracting(ParseException::getDetails)
      .isEqualTo("cause");
  }

  @Test
  void shouldCreateExceptionNullContextNullPosition() {
    ParseException actual = createParseException("message", null, null);

    assertThat(actual).hasMessage("message at null");
    assertThat(actual.getPosition()).isNull();
    assertThat(actual.getDetails()).isNull();
  }

  @Test
  void shouldCreateExceptionNullContext() {
    ParseException actual = createParseException("message", null, new BasicTextPointer(2, 6));

    assertThat(actual).hasMessage("message at null:2:7");
    assertThat(actual.getPosition().line()).isEqualTo(2);
    assertThat(actual.getPosition().lineOffset()).isEqualTo(6);
    assertThat(actual.getDetails()).isNull();
  }

  @Test
  void shouldCreateExceptionNullPosition() {
    ParseException actual = createParseException("message", inputFileContext, null);

    assertThat(actual).hasMessage("message at dir1/dir2/TestFile.abc");
    assertThat(actual.getPosition()).isNull();
    assertThat(actual.getDetails()).isNull();
  }

  @Test
  void shouldParseExceptionWithMarkedYamlEngineExceptionAndWithoutMarker() {
    MarkedYamlEngineException yamlEngineException = mock(MarkedYamlEngineException.class);
    when(yamlEngineException.getProblemMark()).thenReturn(Optional.empty());
    when(yamlEngineException.getMessage()).thenReturn("message");

    when(inputFile.toString()).thenReturn("TestFile");

    ParseException e = ParseException.toParseException("action", inputFileContext, yamlEngineException);
    assertThat(e)
      .hasMessage("Cannot action 'TestFile'")
      .extracting(ParseException::getPosition)
      .isNull();
  }

  @Test
  void shouldParseExceptionWithOtherException() {
    Exception exception = mock(Exception.class);
    when(exception.getMessage()).thenReturn("message");

    when(inputFile.toString()).thenReturn("TestFile");
    when(inputFile.newPointer(2, 0)).thenReturn(new DefaultTextPointer(1, 0));

    ParseException e = ParseException.toParseException("action", inputFileContext, exception);
    assertThat(e)
      .hasMessage("Cannot action 'TestFile'")
      .extracting(ParseException::getPosition)
      .isNull();
  }

  @Test
  void shouldParseExceptionNullInputFile() {
    Exception exception = mock(Exception.class);
    when(exception.getMessage()).thenReturn("message");

    ParseException e = ParseException.toParseException("action", new InputFileContext(null, null), exception);
    assertThat(e)
      .hasMessage("Cannot action 'null'")
      .extracting(ParseException::getPosition)
      .isNull();
  }

  @Test
  void shouldParseExceptionWithMarkedYamlEngineException() {
    MarkedYamlEngineException yamlEngineException = mock(MarkedYamlEngineException.class);
    when(yamlEngineException.getProblemMark()).thenReturn(Optional.of(new Mark("mark", 1, 1, 1, new int[0], 1)));
    when(yamlEngineException.getMessage()).thenReturn("message");

    when(inputFile.toString()).thenReturn("TestFile");
    when(inputFile.newPointer(2, 0)).thenReturn(new DefaultTextPointer(1, 0));

    ParseException e = ParseException.toParseException("action", inputFileContext, yamlEngineException);
    assertThat(e)
      .hasMessage("Cannot action 'TestFile:1:1'")
      .extracting(ParseException::getPosition)
      .isEqualTo(new DefaultTextPointer(1, 0));
  }

  @Test
  void shouldThrowExceptionWhenFailFastIsTrue() {
    MarkedYamlEngineException yamlEngineException = mock(MarkedYamlEngineException.class);
    when(yamlEngineException.getProblemMark()).thenReturn(Optional.of(new Mark("mark", 1, 1, 1, new int[0], 1)));
    when(yamlEngineException.getMessage()).thenReturn("message");

    when(inputFile.toString()).thenReturn("TestFile");
    when(inputFile.newPointer(2, 0)).thenReturn(new DefaultTextPointer(1, 0));
    var sensorContext = createSensorContextFailFast();
    inputFileContext = new InputFileContext(sensorContext, inputFile);
    when(inputFile.newPointer(anyInt(), anyInt())).thenThrow(IllegalArgumentException.class);

    var e = catchException(() -> ParseException.toParseException("action", inputFileContext, yamlEngineException));
    assertThat(e)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Unable to create new pointer for TestFile position 2:0");
  }

  private static SensorContext createSensorContextFailFast() {
    var context = SensorContextTester.create(Paths.get("."));
    var config = new MapSettings().setProperty("sonar.internal.analysis.failFast", true);
    context.setSettings(config);
    return context;
  }
}
