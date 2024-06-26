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
package org.sonar.iac.kubernetes.visitors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.helm.tree.impl.ActionNodeImpl;
import org.sonar.iac.helm.tree.impl.CommandNodeImpl;
import org.sonar.iac.helm.tree.impl.FieldNodeImpl;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.tree.impl.ListNodeImpl;
import org.sonar.iac.helm.tree.impl.PipeNodeImpl;
import org.sonar.iac.helm.tree.impl.TextNodeImpl;
import org.sonar.iac.helm.tree.utils.ValuePath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;
import static org.sonar.iac.common.testing.TextRangeAssert.assertThat;

class SecondaryLocationLocatorTest {
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @Test
  void shouldFindSecondaryLocationsInValuesFile() {
    var inputFileContext = inputFileContextWithTree();

    var locationsInAdditionalFiles = SecondaryLocationLocator.doFindSecondaryLocationsInAdditionalFiles(inputFileContext, range(1, 6, 1, 22));

    assertThat(locationsInAdditionalFiles).hasSize(1);
    assertThat(locationsInAdditionalFiles.get(0).textRange).hasRange(1, 5, 1, 8);
  }

  @Test
  void shouldFindNothingIfRangeIsNotOverlapping() {
    var inputFileContext = inputFileContextWithTree();

    var locationsInAdditionalFiles = SecondaryLocationLocator.doFindSecondaryLocationsInAdditionalFiles(inputFileContext, range(1, 1, 1, 3));
    assertThat(locationsInAdditionalFiles).isEmpty();
  }

  @Test
  void shouldFindNothingIfValuesIsAbsentInValuesFile() {
    var inputFileContext = inputFileContextWithTree();

    var valuesFile = new TestInputFileBuilder("test", ".")
      .setContents("notBar: baz")
      .build();
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));
    var locationsInAdditionalFiles = SecondaryLocationLocator.doFindSecondaryLocationsInAdditionalFiles(inputFileContext, range(1, 6, 1, 22));
    assertThat(locationsInAdditionalFiles).isEmpty();
  }

  @ParameterizedTest
  @MethodSource
  void shouldFindSecondaryLocation(String valuesFileContent, ValuePath valuePath, int expectedStartLine, int expectedStartColumn, int expectedEndLine, int expectedEndColumn)
    throws IOException {
    var textRange = getTextRangeFor(valuesFileContent, valuePath);

    assertThat(textRange).hasRange(expectedStartLine, expectedStartColumn, expectedEndLine, expectedEndColumn);
  }

  static Stream<Arguments> shouldFindSecondaryLocation() {
    return Stream.of(
      Arguments.of("foo: bar", new ValuePath("foo"), 1, 5, 1, 8),
      Arguments.of("foo: bar", new ValuePath("Values", "foo"), 1, 5, 1, 8),
      Arguments.of("""
        foo:
          bar:
            baz: qux""", new ValuePath("foo", "bar", "baz"), 3, 9, 3, 12),
      Arguments.of("""
        foo: bar
        baz: qux""",
        new ValuePath("Values", "foo"), 1, 5, 1, 8),
      Arguments.of("""
        # header

        foo:
          bar:
            bqr: qux
            baz: true""", new ValuePath("foo", "bar", "baz"), 6, 9, 6, 13));
  }

  @ParameterizedTest
  @MethodSource
  void shouldNotFindSecondaryLocation(String valuesFileContent, ValuePath valuePath) throws IOException {
    var textRange = getTextRangeFor(valuesFileContent, valuePath);

    assertThat(textRange).isNull();
  }

  static Stream<Arguments> shouldNotFindSecondaryLocation() {
    return Stream.of(
      Arguments.of("", new ValuePath("foo")),
      Arguments.of("""
        foo:
          bar:
            baz: qux""", new ValuePath("foo", "baz")),
      Arguments.of("""
        foo:
          bar:
            - baz: qux""", new ValuePath("foo", "bar", "baz")),
      Arguments.of("foo: bar", new ValuePath("Chart", "Name")),
      Arguments.of("foo: bar", new ValuePath("Release", "Name")),
      Arguments.of("[1, 2] : foo", new ValuePath("Values", "bar")));
  }

  @Test
  void shouldReturnNullForMissingValuesFile() throws IOException {
    var inputFileContext = inputFileContextWithTree();
    inputFileContext.setAdditionalFiles(Map.of());

    var textRange = SecondaryLocationLocator.toTextRangeInValuesFile(new ValuePath("foo"), inputFileContext);

    assertThat(textRange).isNull();
  }

  @Test
  void shouldReturnEmptyForMissingValuesFile() {
    var inputFileContext = inputFileContextWithTree();
    inputFileContext.setAdditionalFiles(Map.of());

    var secondaryLocations = SecondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(inputFileContext, null);

    assertThat(secondaryLocations).isEmpty();
  }

  @Test
  void shouldReturnEmptyForInputFileContext() {
    var inputFileContext = new InputFileContext(null, null);

    var secondaryLocations = SecondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(inputFileContext, null);

    assertThat(secondaryLocations).isEmpty();
  }

  @Test
  void shouldNotFindSecondaryLocationsWhenIoException() throws IOException {
    var inputFileContext = inputFileContextWithTree();
    var valuesFileMock = mock(InputFile.class);
    when(valuesFileMock.contents()).thenThrow(new IOException("error"));
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFileMock));

    var locationsInAdditionalFiles = SecondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(inputFileContext, range(1, 6, 1, 22));

    assertThat(locationsInAdditionalFiles).isEmpty();
    assertThat(logTester.logs()).anyMatch(line -> line.startsWith("Failed to find secondary locations in additional file"));
  }

  private HelmInputFileContext inputFileContextWithTree() {
    var valuesFile = new TestInputFileBuilder("test", ".")
      .setContents("bar: baz")
      .build();
    HelmInputFileContext inputFileContext;
    try (var ignored = mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("dir1"));
      inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), inputFile("foo.yaml", Path.of("."), "bar: {{ .Values.bar }}", null));
    }
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));

    var fieldNode = new FieldNodeImpl(range(1, 15, 1, 26), List.of("Values", "bar"));
    var command = new CommandNodeImpl(range(1, 8, 1, 19), List.of(fieldNode));
    var pipeNode = new PipeNodeImpl(range(1, 8, 1, 19), List.of(), List.of(command));
    var actionNode = new ActionNodeImpl(range(1, 8, 1, 23), pipeNode);
    var textNode = new TextNodeImpl(range(1, 0, 1, 5), "bar: ");
    ListNodeImpl root = new ListNodeImpl(range(1, 0, 1, 15), List.of(textNode, actionNode));
    var goTemplateTree = new GoTemplateTreeImpl("test", "test", 0, root);
    inputFileContext.setGoTemplateTree(goTemplateTree);
    inputFileContext.setSourceWithComments("bar: {{ .Values.bar }} #1");
    return inputFileContext;
  }

  private TextRange getTextRangeFor(String valuesFileContent, ValuePath valuePath) throws IOException {
    var valuesFile = new TestInputFileBuilder("test", ".")
      .setContents(valuesFileContent)
      .build();
    HelmInputFileContext inputFileContext;
    try (var ignored = mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("dir1"));
      var inputFile = mock(InputFile.class);
      when(inputFile.uri()).thenReturn(Path.of("dir1/templates/something.yaml").toUri());
      inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), inputFile);
    }
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));

    return SecondaryLocationLocator.toTextRangeInValuesFile(valuePath, inputFileContext);
  }

  private SensorContext mockSensorContextWithEnabledFeature() {
    var config = mock(Configuration.class);
    when(config.getBoolean(KubernetesChecksVisitor.ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY)).thenReturn(Optional.of(true));
    var sensorContext = mock(SensorContext.class);
    when(sensorContext.config()).thenReturn(config);
    return sensorContext;
  }
}
