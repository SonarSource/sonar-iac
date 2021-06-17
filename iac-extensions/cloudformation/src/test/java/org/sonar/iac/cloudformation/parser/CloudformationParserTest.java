/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.cloudformation.api.tree.FileTree;
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
    assertThat(tree.root().comments()).hasSize(1);
  }

  @Test
  void test_no_comment_parsing_for_json() {
    when(inputFile.filename()).thenReturn("foo.json");
    FileTree tree = parser.parse("# Comment\na: 1", inputFileContext);
    assertThat(tree.root().comments()).isEmpty();
  }
}
