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
package org.sonar.iac.cloudformation.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CloudformationParserTest {
  private InputFileContext inputFileContext;
  private InputFile inputFile;
  private final CloudformationParser parser = new CloudformationParser();

  @BeforeEach
  void setup() {
    inputFile = mock(InputFile.class);
    inputFileContext = new InputFileContext(mock(SensorContext.class), inputFile);
  }

  @Test
  void test_parse_comments_for_yaml() {
    when(inputFile.filename()).thenReturn("foo.yaml");
    FileTree tree = parser.parse("# Comment\na: 1", inputFileContext);
    assertThat(tree.documents().get(0).metadata().comments()).hasSize(1);
  }

  @Test
  void test_no_comment_parsing_for_json() {
    when(inputFile.filename()).thenReturn("foo.json");
    FileTree tree = parser.parse("# Comment\na: 1", inputFileContext);
    assertThat(tree.documents().get(0).metadata().comments()).isEmpty();
  }
}
