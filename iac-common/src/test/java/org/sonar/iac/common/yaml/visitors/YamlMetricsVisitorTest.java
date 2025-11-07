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
package org.sonar.iac.common.yaml.visitors;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.testing.AbstractMetricsTest;
import org.sonar.iac.common.yaml.YamlParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YamlMetricsVisitorTest extends AbstractMetricsTest {

  @Override
  protected YamlParser treeParser() {
    return new YamlParser();
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetry);
  }

  @Override
  protected String languageKey() {
    return "yaml";
  }

  @Test
  void shouldTestScalarKeyScalarValue() {
    scan("foo: bar");
    assertThat(visitor.linesOfCode()).containsExactly(1);
    assertThat(visitor.commentLines()).isEmpty();
    verifyLinesOfCodeMetricsAndTelemetry(1);
  }

  @Test
  void shouldTestSecondLineScalarKeyScalarValue() {
    scan("\nfoo: bar");
    assertThat(visitor.linesOfCode()).containsExactly(2);
    verifyLinesOfCodeMetricsAndTelemetry(2);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // multilineLiteralScalar
    """
      key: |
        value1
        value2
      """,
    // MultilineLiteralScalarWithSpacesEnding
    """
      key: |
        value1
        value2
          """,
    // multilineFoldedScalar
    """
      key: >
        value1
        value2
      """,
    // yaml mapping
    """
      key:
        - value1
        - value2
      """,
    // json mapping
    """
      {
         "foo": "bar"
      }
      """
  })
  void shouldVerifyMetrics(String code) {
    scan(code);
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3);
    verifyLinesOfCodeMetricsAndTelemetry(1, 2, 3);
  }

  @Test
  void shouldTestScalarKeyScalarValueMultiline() {
    scan("""
      foo:

        bar""");
    assertThat(visitor.linesOfCode()).containsExactly(1, 3);
    assertThat(visitor.commentLines()).isEmpty();
    verifyLinesOfCodeMetricsAndTelemetry(1, 3);
  }

  @Test
  void shouldTestJsonSequence() {
    scan("""
      {
         "foo": [
           "bar"
        ]
      }
      """);
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3, 4, 5);
    verifyLinesOfCodeMetricsAndTelemetry(1, 2, 3, 4, 5);
  }

  @Test
  void commentLines() {
    scan("""
      foo:
        # comment
        #
        bar # comment""");
    assertThat(visitor.linesOfCode()).containsExactly(1, 4);
    assertThat(visitor.commentLines()).containsExactly(2, 4);
    verifyLinesOfCodeMetricsAndTelemetry(1, 4);
  }

  @Test
  void whitespaceLineShouldNotCountAsCode() {
    scan("""
      project: foo



      """);
    assertThat(visitor.linesOfCode()).containsExactly(1);
    verifyLinesOfCodeMetricsAndTelemetry(1);
  }

  @Test
  void raiseParserExceptionWhenAnalysingCorruptFile() throws IOException {
    Tree tree = mock(Tree.class);
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.inputStream()).thenThrow(new IOException("File not found"));
    InputFileContext ctx = new InputFileContext(sensorContext, inputFile);

    ParseException thrown = assertThrows(ParseException.class, () -> visitor.scan(ctx, tree));
    assertThat(thrown.getMessage()).isEqualTo("Can not read file for metric calculation");
    assertThat(thrown.getDetails()).isEqualTo("File not found");
    assertThat(thrown.getPosition()).isNull();
  }

  @Test
  void noSonarLines() {
    scan("""
      # NOSONAR
      key: value # NOSONAR""");
    assertThat(visitor.noSonarLines()).containsExactly(1, 2);
    Set<Integer> nosonarLines = new HashSet<>(Arrays.asList(1, 2));
    verify(noSonarFilter).noSonarInFile(inputFile, nosonarLines);
    verifyLinesOfCodeMetricsAndTelemetry(2);
  }
}
