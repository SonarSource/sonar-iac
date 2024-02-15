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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.helm.tree.impl.ActionNodeImpl;
import org.sonar.iac.helm.tree.impl.CommandNodeImpl;
import org.sonar.iac.helm.tree.impl.FieldNodeImpl;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.tree.impl.ListNodeImpl;
import org.sonar.iac.helm.tree.impl.PipeNodeImpl;
import org.sonar.iac.helm.tree.impl.TextNodeImpl;
import org.sonar.iac.helm.tree.utils.ValuePath;

import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;

class AdjustableChecksVisitorTest {
  @Test
  void shouldFindSecondaryLocationsInValuesFile() throws IOException {
    var adjustableChecksVisitor = new AdjustableChecksVisitor(Mockito.mock(Checks.class), null, null, new YamlParser());

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

    var adjustableCheckContext = (AdjustableChecksVisitor.AdjustableContextAdapter) adjustableChecksVisitor.context(null);

    var locationsInAdditionalFiles = adjustableCheckContext.findSecondaryLocationsInAdditionalFiles(inputFileContext, TextRanges.range(1, 6, 1, 22));

    Assertions.assertThat(locationsInAdditionalFiles).hasSize(1);
    TextRangeAssert.assertThat(locationsInAdditionalFiles.get(0).textRange).hasRange(1, 5, 1, 8);

    locationsInAdditionalFiles = adjustableCheckContext.findSecondaryLocationsInAdditionalFiles(inputFileContext, TextRanges.range(1, 1, 1, 3));
    Assertions.assertThat(locationsInAdditionalFiles).isEmpty();

    valuesFile = new TestInputFileBuilder("test", ".")
      .setContents("notBar: baz")
      .build();
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));
    locationsInAdditionalFiles = adjustableCheckContext.findSecondaryLocationsInAdditionalFiles(inputFileContext, TextRanges.range(1, 6, 1, 22));
    Assertions.assertThat(locationsInAdditionalFiles).isEmpty();
  }

  @ParameterizedTest
  @MethodSource
  void shouldFindSecondaryLocation(String valuesFileContent, ValuePath valuePath, int expectedStartLine, int expectedStartColumn, int expectedEndLine, int expectedEndColumn)
    throws IOException {
    var textRange = getTextRangeFor(valuesFileContent, valuePath);

    TextRangeAssert.assertThat(textRange).hasRange(expectedStartLine, expectedStartColumn, expectedEndLine, expectedEndColumn);
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

    TextRangeAssert.assertThat(textRange).isNull();
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
    var adjustableChecksVisitor = new AdjustableChecksVisitor(Mockito.mock(Checks.class), null, null, new YamlParser());
    var adjustableCheckContext = (AdjustableChecksVisitor.AdjustableContextAdapter) adjustableChecksVisitor.context(null);
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), null);
    inputFileContext.setAdditionalFiles(Map.of());

    var textRange = adjustableCheckContext.toTextRangeInValuesFile(new ValuePath("foo"), inputFileContext);

    TextRangeAssert.assertThat(textRange).isNull();
  }

  @Test
  void shouldReturnEmptyForMissingValuesFile() {
    var adjustableChecksVisitor = new AdjustableChecksVisitor(Mockito.mock(Checks.class), null, null, new YamlParser());
    var adjustableCheckContext = (AdjustableChecksVisitor.AdjustableContextAdapter) adjustableChecksVisitor.context(null);
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), null);
    inputFileContext.setAdditionalFiles(Map.of());

    var locations = adjustableCheckContext.maybeFindSecondaryLocationsInAdditionalFiles(inputFileContext, null);

    Assertions.assertThat(locations).isEmpty();
  }

  @Test
  void shouldNotFindSecondaryLocationsWhenIoException() throws IOException {
    var adjustableChecksVisitor = new AdjustableChecksVisitor(Mockito.mock(Checks.class), null, null, new YamlParser());
    var inputFile = Mockito.mock(InputFile.class);
    Mockito.when(inputFile.contents()).thenThrow(new IOException("error"));
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), inputFile);
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", Mockito.mock(InputFile.class)));
    inputFileContext.setGoTemplateTree(Mockito.mock(GoTemplateTreeImpl.class));
    var adjustableCheckContext = (AdjustableChecksVisitor.AdjustableContextAdapter) adjustableChecksVisitor.context(null);

    var locationsInAdditionalFiles = adjustableCheckContext.maybeFindSecondaryLocationsInAdditionalFiles(inputFileContext, TextRanges.range(1, 6, 1, 22));

    Assertions.assertThat(locationsInAdditionalFiles).isEmpty();
  }

  @CheckForNull
  private TextRange getTextRangeFor(String valuesFileContent, ValuePath valuePath) throws IOException {
    var adjustableChecksVisitor = new AdjustableChecksVisitor(Mockito.mock(Checks.class), null, null, new YamlParser());
    var adjustableCheckContext = (AdjustableChecksVisitor.AdjustableContextAdapter) adjustableChecksVisitor.context(null);

    var valuesFile = new TestInputFileBuilder("test", ".")
      .setContents(valuesFileContent)
      .build();
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), null);
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));

    return adjustableCheckContext.toTextRangeInValuesFile(valuePath, inputFileContext);
  }

  private SensorContext mockSensorContextWithEnabledFeature() {
    var config = Mockito.mock(Configuration.class);
    Mockito.when(config.getBoolean(AdjustableChecksVisitor.ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY)).thenReturn(Optional.of(true));
    var sensorContext = Mockito.mock(SensorContext.class);
    Mockito.when(sensorContext.config()).thenReturn(config);
    return sensorContext;
  }
}
