/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.cloudformation.visitors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.cloudformation.parser.CloudformationParser;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.testing.AbstractMetricsTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class CloudformationMetricsVisitorTest extends AbstractMetricsTest {

  @Override
  protected TreeParser treeParser() {
    return new CloudformationParser();
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new CloudformationMetricsVisitor(fileLinesContextFactory, noSonarFilter);
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

  // FIXME SONARIAC-80: Do not add line to range if scalar ends with new line
  @Test
  void multiline_scalar_value() {
    scan("" +
      "key: |\n" +
      "  value\n" +
      "  value\n");
    assertThat(visitor.linesOfCode()).containsExactly(1,2,3,4);
  }

  @Test
  void yaml_mapping() {
    scan("" +
      "key:\n" +
      "  - value\n" +
      "  - value\n");
    assertThat(visitor.linesOfCode()).containsExactly(1,2,3);
  }

  @Test
  void scalar_key_scalar_value_multiline() {
    scan("" +
      "foo:\n" +
      "\n" +
      "   bar");
    assertThat(visitor.linesOfCode()).containsExactly(1,3);
    assertThat(visitor.commentLines()).isEmpty();
  }

  // TODO: SONARIAC-82 Lines which contain only brackets should also be counted for metrics
  @Test
  void json_mapping() {
    scan("" +
      "{\n" +
      "  \"foo\": \"bar\"\n" +
      "}");
    assertThat(visitor.linesOfCode()).containsExactly(2);
  }

  // TODO: SONARIAC-82 Lines which contain only brackets should also be counted for metrics
  @Test
  void json_sequence() {
    scan("" +
      "{\n" +
      "  \"foo\": [\n" +
      "    \"bar\"\n" +
      "  ]\n" +
      "}");
    assertThat(visitor.linesOfCode()).containsExactly(2,3);
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
  void noSonarLines() {
    scan("" +
      "# NOSONAR\n" +
      "key: value # NOSONAR");
    assertThat(visitor.noSonarLines()).containsExactly(1, 2);
    Set<Integer> nosonarLines = new HashSet<>(Arrays.asList(1, 2));
    verify(noSonarFilter).noSonarInFile(inputFile, nosonarLines);
  }
}
