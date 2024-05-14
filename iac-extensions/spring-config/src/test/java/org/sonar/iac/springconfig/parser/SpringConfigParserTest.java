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
package org.sonar.iac.springconfig.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.iac.springconfig.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;

class SpringConfigParserTest {
  private final SpringConfigParser parser = new SpringConfigParser();

  private final InputFileContext inputFilePropertiesContext = createInputFileContextMock("application.properties");
  private final InputFileContext inputFileYamlContext = createInputFileContextMock("application.yaml", YamlLanguage.KEY);

  @Test
  void shouldParseSimpleYaml() {
    String source = """
      a:
        b: c""";
    File file = (File) parser.parse(source, inputFileYamlContext);
    assertThat(file.profiles().get(0).properties()).hasSize(1);
  }

  @Test
  void shouldParseSimpleProperties() {
    File tree = (File) parser.parse("foo=bar", inputFilePropertiesContext);
    assertThat(tree.profiles().get(0).properties()).hasSize(1);
  }

  @Test
  void shouldThrowParseExceptionOnEmptyYamlFile() {
    // the parser itself fails on an empty yaml file
    // in an actual analysis empty files are never parsed because we filter them beforehand
    assertThatThrownBy(() -> parser.parse("", inputFileYamlContext))
      .isInstanceOf(ParseException.class)
      .hasMessage("Unexpected empty nodes list while converting file");
  }

  @Test
  void shouldThrowParseExceptionOnPropertiesFile() {
    assertThatThrownBy(() -> parser.parse("=bar", inputFilePropertiesContext))
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse, extraneous input '=' expecting {<EOF>, COMMENT, WHITESPACE, CHARACTER} at dir1/dir2/application.properties:1:1");
  }

  @Test
  void shouldThrowExceptionWithWrongExtension() {
    assertThatThrownBy(() -> parser.parse("", createInputFileContextMock("foo.ext")))
      .isInstanceOf(ParseException.class)
      .hasMessage("Unsupported file extension at dir1/dir2/foo.ext");
  }

  @Test
  void shouldThrowExceptionWithNoInputFileContext() {
    assertThatThrownBy(() -> parser.parse("", null))
      .isInstanceOf(ParseException.class)
      .hasMessage("Unsupported file extension at null");
  }

}
