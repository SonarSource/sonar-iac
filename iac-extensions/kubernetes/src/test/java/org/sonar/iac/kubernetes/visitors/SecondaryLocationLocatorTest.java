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
package org.sonar.iac.kubernetes.visitors;

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
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.filesystem.FileSystemUtils;
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
import static org.sonar.iac.common.filesystem.FileSystemUtils.retrieveHelmProjectFolder;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;
import static org.sonar.iac.common.testing.TextRangeAssert.assertThat;

class SecondaryLocationLocatorTest {
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private static final Path BASE_DIR = Path.of("dir1");

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
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", "notBar: baz"));
    var locationsInAdditionalFiles = SecondaryLocationLocator.doFindSecondaryLocationsInAdditionalFiles(inputFileContext, range(1, 6, 1, 22));
    assertThat(locationsInAdditionalFiles).isEmpty();
  }

  @ParameterizedTest
  @MethodSource
  void shouldFindSecondaryLocation(String valuesFileContent, ValuePath valuePath, int expectedStartLine, int expectedStartColumn, int expectedEndLine, int expectedEndColumn) {
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
  void shouldNotFindSecondaryLocation(String valuesFileContent, ValuePath valuePath) {
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
  void shouldReturnNullForMissingValuesFile() {
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

  private HelmInputFileContext inputFileContextWithTree() {
    HelmInputFileContext inputFileContext;
    try (var ignored = mockStatic(FileSystemUtils.class)) {
      when(retrieveHelmProjectFolder(any(), any())).thenReturn(BASE_DIR);
      inputFileContext = new HelmInputFileContext(
        mockSensorContextWithEnabledFeature(),
        inputFile("template/foo.yaml", BASE_DIR, "bar: {{ .Values.bar }}",
          null),
        null);
    }
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", "bar: baz"));

    var fieldNode = new FieldNodeImpl(() -> range(1, 15, 1, 26), List.of("Values", "bar"));
    var command = new CommandNodeImpl(() -> range(1, 8, 1, 19), List.of(fieldNode));
    var pipeNode = new PipeNodeImpl(() -> range(1, 8, 1, 19), List.of(), List.of(command));
    var actionNode = new ActionNodeImpl(() -> range(1, 8, 1, 23), pipeNode);
    var textNode = new TextNodeImpl(() -> range(1, 0, 1, 5), "bar: ");
    ListNodeImpl root = new ListNodeImpl(() -> range(1, 0, 1, 15), List.of(textNode, actionNode));
    var goTemplateTree = new GoTemplateTreeImpl("test", "test", 0, root);
    inputFileContext.setGoTemplateTree(goTemplateTree);
    inputFileContext.setSourceWithComments("bar: {{ .Values.bar }} #1");
    return inputFileContext;
  }

  private TextRange getTextRangeFor(String valuesFileContent, ValuePath valuePath) {
    HelmInputFileContext inputFileContext;
    try (var ignored = mockStatic(FileSystemUtils.class)) {
      when(retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("dir1"));
      var inputFile = mock(InputFile.class);
      when(inputFile.uri()).thenReturn(Path.of("dir1/templates/something.yaml").toUri());
      inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), inputFile, null);
    }
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFileContent));

    return SecondaryLocationLocator.toTextRangeInValuesFile(valuePath, inputFileContext);
  }

  private SensorContext mockSensorContextWithEnabledFeature() {
    var config = mock(Configuration.class);
    when(config.getBoolean(KubernetesChecksVisitor.ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY)).thenReturn(Optional.of(true));
    var sensorContext = mock(SensorContext.class);
    when(sensorContext.config()).thenReturn(config);

    FileSystem fileSystem = mock(FileSystem.class);
    when(fileSystem.baseDir()).thenReturn(BASE_DIR.toFile());
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    return sensorContext;
  }
}
