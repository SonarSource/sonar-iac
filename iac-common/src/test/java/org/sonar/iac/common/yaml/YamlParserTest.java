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
package org.sonar.iac.common.yaml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.FileTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
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
    assertThat(exception.getMessage()).isEqualTo("Recursive node found\n" +
      " in reader, line 1, column 11:\n" +
      "    some_key: &some_anchor\n" +
      "              ^\n");
  }
}
