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
package org.sonar.iac.cloudformation.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTreeImpl;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;
import org.sonar.iac.common.yaml.tree.TupleTreeImpl;

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
    assertThat(((MappingTreeImpl) tree.documents().get(0)).elements().get(0).key().metadata().comments()).hasSize(1);
  }

  @Test
  void test_no_comment_parsing_for_json() {
    when(inputFile.filename()).thenReturn("foo.json");
    FileTree tree = parser.parse("# Comment\na: 1", inputFileContext);
    assertThat(tree.documents().get(0).metadata().comments()).isEmpty();
  }

  @Test
  void shouldNotFailInImplicitNullInYaml() {
    when(inputFile.filename()).thenReturn("foo.yaml");
    FileTree tree = parser.parse("""
      foo:
        a: null
        c: ~
        d:  # implicit null --> parsing error""", inputFileContext);
    MappingTreeImpl foo = (MappingTreeImpl) tree.documents().get(0);
    TupleTreeImpl d = (TupleTreeImpl) (foo.elements().get(0).value().children().get(2));
    assertThat(((ScalarTreeImpl) d.value()).value()).isEmpty();
  }
}
