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
package org.sonar.iac.kubernetes.plugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.predicates.DefaultFilePredicates;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class EvaluatedHelmTemplateCleanerTest {

  private final InputFile inputFile = mock(InputFile.class);
  private final SensorContext sensorContext = mock(SensorContext.class);
  private final InputFileContext inputFileContext = new InputFileContext(sensorContext, inputFile);

  private LocationShifter locationShifter;

  @BeforeEach
  void setup() throws URISyntaxException {
    var fs = mock(FileSystem.class);
    when(sensorContext.fileSystem()).thenReturn(fs);
    when(fs.predicates()).thenReturn(new DefaultFilePredicates(Path.of(".")));
    when(inputFile.filename()).thenReturn("foo.yaml");
    when(inputFile.toString()).thenReturn("chart/templates/foo.yaml");
    when(inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
    locationShifter = new LocationShifter();
  }

  @Test
  void shouldRemoveEmptyLinesAfterEvaluation() {
    String evaluated = code(
      "apiVersion: apps/v1 #1",
      "kind: StatefulSet #2",
      "metadata: #3",
      "  name: helm-chart-sonarqube-dce-search #4",
      "spec: #5",
      "  livenessProbe: #6",
      "    exec: #7",
      "      command: #8",
      "        - sh #9",
      "        - -c #10",
      "        #14",
      "        - | #15",
      "          bar #16 #17",
      "    initialDelaySeconds: 60 #18",
      "  #19");
    var expected = code(
      "apiVersion: apps/v1",
      "kind: StatefulSet",
      "metadata:",
      "  name: helm-chart-sonarqube-dce-search",
      "spec:",
      "  livenessProbe:",
      "    exec:",
      "      command:",
      "        - sh",
      "        - -c",
      "        - |",
      "          bar",
      "    initialDelaySeconds: 60",
      "");
    var actual = cleanSource(evaluated);

    assertThat(actual).isEqualTo(expected);

    assertLineMapping(
      1, 1,
      10, 10,
      11, 14,
      12, 15,
      13, 16,
      14, 18,
      15, 19);
  }

  @Test
  void shouldRemoveNewDocumentAfterEvaluation() {
    var evaluated = code(
      "--- #5",
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9");
    var expected = code(
      "---",
      "apiVersion: v1",
      "kind: Pod",
      "metadata:",
      "spec:");
    var actual = cleanSource(evaluated);

    assertThat(actual).isEqualTo(expected);

    assertLineMapping(
      1, 5,
      2, 6,
      3, 7,
      4, 8,
      5, 9);
  }

  @Test
  void shouldRemoveLineNumberCommentForNewDocumentAtEndAfterEvaluation() {
    var evaluated = code(
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9",
      "--- #12");
    var expected = code(
      "apiVersion: v1",
      "kind: Pod",
      "metadata:",
      "spec:",
      "---");
    var actual = cleanSource(evaluated);

    assertThat(actual).isEqualTo(expected);

    assertLineMapping(
      1, 6,
      2, 7,
      3, 8,
      4, 9,
      5, 12);
  }

  @Test
  void shouldRemoveLineNumberCommentForEndDocumentAfterEvaluation() {
    var evaluated = code(
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9",
      "... #10");
    var expected = code(
      "apiVersion: v1",
      "kind: Pod",
      "metadata:",
      "spec:",
      "...");
    var actual = cleanSource(evaluated);

    assertThat(actual).isEqualTo(expected);

    assertLineMapping(
      1, 6,
      2, 7,
      3, 8,
      4, 9,
      5, 10);
  }

  private String cleanSource(String evaluated) {
    return EvaluatedHelmTemplateCleaner.cleanSource(evaluated, inputFileContext, locationShifter);
  }

  private void assertLineMapping(int... numbers) {
    if (numbers.length % 2 == 1) {
      throw new RuntimeException("The even number of arguments is expected");
    }
    for (int i = 0; i < numbers.length; i = i + 2) {
      var textRange = getComputeShiftedLocation(numbers[i], 0, numbers[i], 0);
      assertThat(textRange).hasRange(numbers[i + 1], 0, numbers[i + 1], 0);
    }
  }

  private TextRange getComputeShiftedLocation(int startLine, int startLineOffset, int endLine, int endLineOffset) {
    return locationShifter.computeShiftedLocation(inputFileContext, new TextRange(new TextPointer(startLine, startLineOffset), new TextPointer(endLine, endLineOffset)));
  }
}
