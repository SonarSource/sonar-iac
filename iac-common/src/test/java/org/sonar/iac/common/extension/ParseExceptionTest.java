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
package org.sonar.iac.common.extension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParseExceptionTest {

  private final RuntimeException cause = new RuntimeException("cause");
  private final TextPointer position = new BasicTextPointer(1, 2);
  private InputFile inputFile;

  @BeforeEach
  public void init() {
    inputFile = mock(InputFile.class);
    when(inputFile.toString()).thenReturn("dir1/dir2/TestFile.abc");
  }

  @Test
  void shouldCreateException() {
    ParseException actual = ParseException.throwGeneralParseException("action", inputFile, cause, position);

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
    ParseException actual = ParseException.throwGeneralParseException("action", null, cause, position);

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
    ParseException actual = ParseException.throwGeneralParseException("action", inputFile, cause, null);

    assertThat(actual)
      .hasMessage("Cannot action 'dir1/dir2/TestFile.abc'")
      .extracting(ParseException::getPosition)
      .isNull();
    assertThat(actual)
      .extracting(ParseException::getDetails)
      .isEqualTo("cause");
  }
}
