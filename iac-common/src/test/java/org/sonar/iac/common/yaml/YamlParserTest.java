/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.common.yaml;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YamlParserTest {

  private InputFileContext inputFileContext;
  private final InputFile inputFile = mock(InputFile.class);

  private final YamlParser parser = new YamlParser();

  @BeforeEach
  void setup() {
    inputFileContext = new InputFileContext(mock(SensorContext.class), inputFile);
    when(inputFile.filename()).thenReturn("foo.yaml");
  }

  @Test
  void parse_empty_file() {
    Exception exception = assertThrows(ParseException.class, () -> parser.parse("", inputFileContext));
    assertThat(exception.getMessage()).isEqualTo("Unexpected empty nodes list while converting file");
  }

  @Test
  void parse_json_file() {
    when(inputFile.filename()).thenReturn("foo.json");
    assertThatNoException().isThrownBy(() -> parser.parse("foo: {bar: 1234}", inputFileContext));
  }

  @Test
  void parse_without_context() {
    assertThatNoException().isThrownBy(() -> parser.parse("# comment", null));
  }

  @Test
  void parse_recursion() {
    Exception exception = assertThrows(ParserException.class, () -> parser.parse("some_key: &some_anchor\n  sub_key: *some_anchor", inputFileContext));
    assertThat(exception.getMessage()).isEqualTo("""
      Recursive node found
       in reader, line 1, column 11:
          some_key: &some_anchor
                    ^
      """);
  }

  @Test
  void testSnakeyamlSucceeding() {
    // language=yaml
    var validYaml = """
      foo:
        bar: baz
      # block comment before
      ---
      # block comment after
      foo: bar
      --- !foo bar
      --- >
        foo
      --- |
        foo
      """;

    assertThatNoException().isThrownBy(() -> parser.parse(validYaml, inputFileContext));
  }

  @ParameterizedTest
  @ValueSource(
    // language=yaml
    strings = {
      "--- ###",
      "--- foo ###",
      "--- foo: bar ###",
    })
  void testSnakeyamlFailing(String input) {
    assertThatThrownBy(() -> parser.parse(input, inputFileContext))
      .isOfAnyClassIn(ParserException.class, ScannerException.class, ClassCastException.class);
  }

  @ParameterizedTest
  @CsvSource(
    value = {
      "single-tab-indentation-without-nesting.json, true",
      "single-tab-indentation.yaml, false",
      "double-tab-indentation.yaml, false",
      "double-tab-indentation.json, false"
    })
  void snakeYamlParsingFromResourceFileShouldHaveExpectedBehavior(String filename, boolean shouldParse) throws IOException {
    InputFile customInputFile = IacTestUtils.inputFile("parser/" + filename, "");
    if (shouldParse) {
      assertThatNoException().isThrownBy(() -> parser.parse(customInputFile.contents(), inputFileContext));
    } else {
      var content = customInputFile.contents();
      assertThatThrownBy(() -> parser.parse(content, inputFileContext))
        .isOfAnyClassIn(ParserException.class, ScannerException.class, ClassCastException.class);
    }
  }

}
