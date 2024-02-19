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
import javax.annotation.CheckForNull;
import org.assertj.core.api.Assertions;
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
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.helm.tree.impl.ActionNodeImpl;
import org.sonar.iac.helm.tree.impl.CommandNodeImpl;
import org.sonar.iac.helm.tree.impl.FieldNodeImpl;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.tree.impl.ListNodeImpl;
import org.sonar.iac.helm.tree.impl.PipeNodeImpl;
import org.sonar.iac.helm.tree.impl.TextNodeImpl;
import org.sonar.iac.helm.tree.utils.ValuePath;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;
import static org.sonar.iac.common.testing.TextRangeAssert.assertThat;

class SecondaryLocationLocatorTest {
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private final SecondaryLocationLocator secondaryLocationLocator = new SecondaryLocationLocator(new YamlParser());

  @Test
  void shouldFindSecondaryLocationsInValuesFile() {
    var inputFileContext = inputFileContextWithTree();

    var locationsInAdditionalFiles = secondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(inputFileContext, TextRanges.range(1, 6, 1, 22));

    Assertions.assertThat(locationsInAdditionalFiles).hasSize(1);
    assertThat(locationsInAdditionalFiles.get(0).textRange).hasRange(1, 5, 1, 8);
  }

  @Test
  void shouldFindNothingIfRangeIsNotOverlapping() {
    var inputFileContext = inputFileContextWithTree();

    var locationsInAdditionalFiles = secondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(inputFileContext, TextRanges.range(1, 1, 1, 3));
    Assertions.assertThat(locationsInAdditionalFiles).isEmpty();
  }

  @Test
  void shouldFindNothingIfValuesIsAbsentInValuesFile() {
    var inputFileContext = inputFileContextWithTree();

    var valuesFile = new TestInputFileBuilder("test", ".")
      .setContents("notBar: baz")
      .build();
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));
    var locationsInAdditionalFiles = secondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(inputFileContext, TextRanges.range(1, 6, 1, 22));
    Assertions.assertThat(locationsInAdditionalFiles).isEmpty();
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
      Arguments.of(code(
        "foo:",
        "  bar:",
        "    baz: qux"), new ValuePath("foo", "bar", "baz"), 3, 9, 3, 12),
      Arguments.of(code(
        "foo: bar",
        "baz: qux"),
        new ValuePath("Values", "foo"), 1, 5, 1, 8),
      Arguments.of(code(
        "# header",
        "",
        "foo:",
        "  bar:",
        "    bqr: qux",
        "    baz: true"), new ValuePath("foo", "bar", "baz"), 6, 9, 6, 13));
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
      Arguments.of(code(
        "foo:",
        "  bar:",
        "    baz: qux"), new ValuePath("foo", "baz")),
      Arguments.of(code("foo:",
        "  bar:",
        "    - baz: qux"), new ValuePath("foo", "bar", "baz")),
      Arguments.of("foo: bar", new ValuePath("Chart", "Name")),
      Arguments.of("foo: bar", new ValuePath("Release", "Name")),
      Arguments.of("[1, 2] : foo", new ValuePath("Values", "bar")));
  }

  @Test
  void shouldReturnNullForMissingValuesFile() throws IOException {
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), null);
    inputFileContext.setAdditionalFiles(Map.of());

    var textRange = secondaryLocationLocator.toTextRangeInValuesFile(new ValuePath("foo"), inputFileContext);

    assertThat(textRange).isNull();
  }

  @Test
  void shouldReturnEmptyForMissingValuesFile() {
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), null);
    inputFileContext.setAdditionalFiles(Map.of());

    var secondaryLocations = secondaryLocationLocator.maybeFindSecondaryLocationsInAdditionalFiles(inputFileContext, null);

    Assertions.assertThat(secondaryLocations).isEmpty();
  }

  @Test
  void shouldNotFindSecondaryLocationsWhenIoException() throws IOException {
    var inputFile = mock(InputFile.class);
    when(inputFile.contents()).thenThrow(new IOException("error"));
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), inputFile);
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", mock(InputFile.class)));
    inputFileContext.setGoTemplateTree(mock(GoTemplateTreeImpl.class));

    var locationsInAdditionalFiles = secondaryLocationLocator.maybeFindSecondaryLocationsInAdditionalFiles(inputFileContext, TextRanges.range(1, 6, 1, 22));

    Assertions.assertThat(locationsInAdditionalFiles).isEmpty();
    Assertions.assertThat(logTester.logs()).anyMatch(line -> line.startsWith("Failed to find secondary locations in additional file"));
  }

  private HelmInputFileContext inputFileContextWithTree() {
    var valuesFile = new TestInputFileBuilder("test", ".")
      .setContents("bar: baz")
      .build();
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), inputFile("foo.yaml", Path.of("."), "bar: {{ .Values.bar }}", null));
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));

    var fieldNode = new FieldNodeImpl(15, 11, List.of("Values", "bar"));
    var command = new CommandNodeImpl(8, 11, List.of(fieldNode));
    var pipeNode = new PipeNodeImpl(8, 11, List.of(), List.of(command));
    var actionNode = new ActionNodeImpl(8, 15, pipeNode);
    var textNode = new TextNodeImpl(0, 5, "bar: ");
    ListNodeImpl root = new ListNodeImpl(0, 15, List.of(textNode, actionNode));
    var goTemplateTree = new GoTemplateTreeImpl("test", "test", 0, root);
    inputFileContext.setGoTemplateTree(goTemplateTree);

    return inputFileContext;
  }

  @CheckForNull
  private TextRange getTextRangeFor(String valuesFileContent, ValuePath valuePath) throws IOException {
    var valuesFile = new TestInputFileBuilder("test", ".")
      .setContents(valuesFileContent)
      .build();
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), null);
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));

    return secondaryLocationLocator.toTextRangeInValuesFile(valuePath, inputFileContext);
  }

  private SensorContext mockSensorContextWithEnabledFeature() {
    var config = mock(Configuration.class);
    when(config.getBoolean(AdjustableChecksVisitor.ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY)).thenReturn(Optional.of(true));
    var sensorContext = mock(SensorContext.class);
    when(sensorContext.config()).thenReturn(config);
    return sensorContext;
  }
}
