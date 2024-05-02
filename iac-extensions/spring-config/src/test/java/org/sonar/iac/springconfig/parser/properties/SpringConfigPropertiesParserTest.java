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
package org.sonar.iac.springconfig.parser.properties;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.springconfig.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

class SpringConfigPropertiesParserTest {

  @Test
  void shouldParseExampleFile() {
    var code = """
      foo=bar""";
    var parser = new SpringConfigPropertiesParser();
    InputFileContext inputFileContext = IacTestUtils.createInputFileContextMock("foo.properties");

    var tree = (File) parser.parse(code, inputFileContext);

    var tuple = tree.profiles().get(0).properties().get(0);
    assertThat(tuple.key().value().value()).isEqualTo("foo");
    assertThat(tuple.value().value().value()).isEqualTo("bar");
  }

  @Test
  void shouldThrowExceptionWhenKeyContainsExclamationMark() {
    var code = """
      foo!=bar""";
    var parser = new SpringConfigPropertiesParser();
    InputFileContext inputFileContext = IacTestUtils.createInputFileContextMock("foo.properties");

    var exception = catchException(() -> parser.parse(code, inputFileContext));

    assertThat(exception)
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse, mismatched input '!' expecting {<EOF>, NEWLINE, DELIMITER} at dir1/dir2/foo.properties:1:4");
  }
}
