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
package org.sonar.iac.common.yaml.visitors;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
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
    return new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter);
  }

  @Test
  void scalar_key_scalar_value() {
    scan("foo: bar");
    assertThat(visitor.linesOfCode()).containsExactly(1);
    assertThat(visitor.commentLines()).isEmpty();
  }

  @Test
  void second_line_scalar_key_scalar_value() {
    scan("\nfoo: bar");
    assertThat(visitor.linesOfCode()).containsExactly(2);
  }

  @Test
  void multiline_literal_scalar() {
    scan("" +
      "key: |\n" +
      "  value1\n" +
      "  value2\n");
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3);
  }

  @Test
  void multiline_literal_scalar_with_spaces_ending() {
    scan("" +
      "key: |\n" +
      "  value1\n" +
      "  value2\n    ");
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3);
  }

  @Test
  void multiline_folded_scalar() {
    scan("" +
      "key: >\n" +
      "  value1\n" +
      "  value2\n");
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3);
  }

  @Test
  void yaml_mapping() {
    scan("" +
      "key:\n" +
      "  - value\n" +
      "  - value\n");
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3);
  }

  @Test
  void scalar_key_scalar_value_multiline() {
    scan("" +
      "foo:\n" +
      "\n" +
      "   bar");
    assertThat(visitor.linesOfCode()).containsExactly(1, 3);
    assertThat(visitor.commentLines()).isEmpty();
  }

  @Test
  void json_mapping() {
    scan("" +
      "{\n" +
      "  \"foo\": \"bar\"\n" +
      "}\n");
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3);
  }

  @Test
  void json_sequence() {
    scan("" +
      "{\n" +
      "  \"foo\": [\n" +
      "    \"bar\"\n" +
      "  ]\n" +
      "}\n");
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  void commentLines() {
    scan("" +
      "foo:\n" +
      "  # comment\n" +
      "  #\n" +
      "  bar # comment");
    assertThat(visitor.linesOfCode()).containsExactly(1, 4);
    assertThat(visitor.commentLines()).containsExactly(2, 4);
  }

  @Test
  void whitespaceLineShouldNotCountAsCode() {
    scan("project: foo",
      " ", " ", " ");
    assertThat(visitor.linesOfCode()).containsExactly(1);
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
    scan("" +
      "# NOSONAR\n" +
      "key: value # NOSONAR");
    assertThat(visitor.noSonarLines()).containsExactly(1, 2);
    Set<Integer> nosonarLines = new HashSet<>(Arrays.asList(1, 2));
    verify(noSonarFilter).noSonarInFile(inputFile, nosonarLines);
  }
}
