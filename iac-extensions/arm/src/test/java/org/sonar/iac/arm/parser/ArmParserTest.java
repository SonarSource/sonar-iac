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
package org.sonar.iac.arm.parser;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArmParserTest {

  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseEmptyJson() {
    File tree = (File) parser.parse("{}", null);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.children()).isEmpty();
    assertThat(tree.parent()).isNull();
    Throwable throwable = catchThrowable(tree::textRange);
    assertThat(throwable).isInstanceOf(IllegalArgumentException.class);

    // temp test for coverage
    tree.setParent(tree);
    assertThat(tree.parent()).isSameAs(tree);
  }

  @Test
  void shouldThrowExceptionWhenParseError() {
    Throwable throwable = catchThrowable(() -> parser.parse("{", null));
    assertThat(throwable)
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to parse");
  }

  @Test
  void shouldThrowExceptionWithFileNameWhenParseError() {
    InputFile inputFile = mock(InputFile.class);
    InputFileContext inputFileContext = new InputFileContext(mock(SensorContext.class), inputFile);
    when(inputFile.filename()).thenReturn("foo.json");

    Throwable throwable = catchThrowable(() -> parser.parse("{", inputFileContext));
    assertThat(throwable)
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to parse foo.json");
  }
}
