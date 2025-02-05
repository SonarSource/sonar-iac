/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.jvmframeworkconfig.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.iac.jvmframeworkconfig.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;

class JvmFrameworkConfigParserTest {
  private final JvmFrameworkConfigParser parser = new JvmFrameworkConfigParser();

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
