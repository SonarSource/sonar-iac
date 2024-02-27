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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.helm.tree.impl.ActionNodeImpl;
import org.sonar.iac.helm.tree.impl.CommandNodeImpl;
import org.sonar.iac.helm.tree.impl.FieldNodeImpl;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.tree.impl.ListNodeImpl;
import org.sonar.iac.helm.tree.impl.PipeNodeImpl;
import org.sonar.iac.helm.tree.impl.TextNodeImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;
import static org.sonar.iac.kubernetes.KubernetesAssertions.assertThat;

final class LocationShifterTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  @TempDir
  private static File tmpDir;

  private static File baseDir;
  private static SensorContextTester context;
  private static InputFileContext ctx;
  private final LocationShifter shifter = new LocationShifter();

  @BeforeAll
  static void init() throws IOException {
    baseDir = tmpDir.toPath().toRealPath().resolve("test-project").toFile();
    FileUtils.forceMkdir(baseDir);
    context = SensorContextTester.create(baseDir);
    InputFile file = createInputFile("primaryFile");
    ctx = new InputFileContext(context, file);
  }

  @Test
  void shouldReturnShiftedRange() {
    setLinesSizes(ctx, 5, 10);
    shifter.addShiftedLine(ctx, 1, 2);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, range(1, 1, 1, 3));
    assertThat(shiftedRange).hasRange(2, 0, 2, 10);
  }

  @Test
  void shouldReturnLastLineIfNoShiftStored() {
    setLinesSizes(ctx, 5);
    TextRange originalRange = range(1, 1, 1, 3);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, originalRange);
    assertThat(shiftedRange).hasRange(1, 0, 1, 5);
  }

  @Test
  void shouldOverridePreviousShift() {
    setLinesSizes(ctx, 5, 10);
    shifter.addShiftedLine(ctx, 1, 5);
    shifter.addShiftedLine(ctx, 1, 2);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, range(1, 1, 1, 3));
    assertThat(shiftedRange).hasRange(2, 0, 2, 10);
  }

  @Test
  void shouldKeepAllShifts() {
    setLinesSizes(ctx, 5, 10, 15, 20);
    shifter.addShiftedLine(ctx, 1, 3);
    shifter.addShiftedLine(ctx, 2, 4);

    TextRange shiftedRange1 = shifter.computeShiftedLocation(ctx, range(1, 1, 1, 3));
    assertThat(shiftedRange1).hasRange(3, 0, 3, 15);

    TextRange shiftedRange2 = shifter.computeShiftedLocation(ctx, range(2, 1, 2, 3));
    assertThat(shiftedRange2).hasRange(4, 0, 4, 20);
  }

  @Test
  void shouldAllowSameTargetLine() {
    setLinesSizes(ctx, 5, 10, 15);
    shifter.addShiftedLine(ctx, 1, 3);
    shifter.addShiftedLine(ctx, 2, 3);

    TextRange shiftedRange1 = shifter.computeShiftedLocation(ctx, range(1, 1, 1, 3));
    assertThat(shiftedRange1).hasRange(3, 0, 3, 15);

    TextRange shiftedRange2 = shifter.computeShiftedLocation(ctx, range(2, 1, 2, 3));
    assertThat(shiftedRange2).hasRange(3, 0, 3, 15);
  }

  @Test
  void shouldShiftStartRangeOnlyForRegisteredLineShift() {
    setLinesSizes(ctx, 5, 10, 15);
    shifter.addShiftedLine(ctx, 1, 2);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, range(1, 1, 3, 3));
    assertThat(shiftedRange).hasRange(2, 0, 3, 15);
  }

  @Test
  void shouldShiftEndRangeOnlyForRegisteredLineShift() {
    setLinesSizes(ctx, 5, 10, 15);
    shifter.addShiftedLine(ctx, 2, 3);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, range(1, 1, 2, 3));
    assertThat(shiftedRange).hasRange(3, 0, 3, 15);
  }

  @Test
  void shouldProvideShiftedRangeOnMultipleLines() {
    setLinesSizes(ctx, 5, 10, 15, 20);
    shifter.addShiftedLine(ctx, 1, 3);
    shifter.addShiftedLine(ctx, 2, 4);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, range(1, 1, 2, 3));
    assertThat(shiftedRange).hasRange(3, 0, 4, 20);
  }

  @Test
  void shouldSkipShiftingIfContextIsNotRecorded() {
    setLinesSizes(ctx, 5, 10, 15);
    shifter.addShiftedLine(ctx, 3, 1);

    InputFileContext differentCtx = new InputFileContext(context, createInputFile("file_2"));

    TextRange shiftedRange = shifter.computeShiftedLocation(differentCtx, range(1, 1, 1, 3));

    assertThat(shiftedRange).hasRange(1, 1, 1, 3);
  }

  @Test
  void shouldNotAccessShiftedRangeOfDifferentContext() {
    InputFileContext differentCtx = new InputFileContext(context, createInputFile("file_2"));

    setLinesSizes(ctx, 5, 10, 15);
    setLinesSizes(differentCtx, 6, 11, 16, 21);
    shifter.addShiftedLine(ctx, 1, 3);
    shifter.addShiftedLine(differentCtx, 2, 4);

    TextRange shiftedRangeCtx1_1 = shifter.computeShiftedLocation(ctx, range(1, 1, 1, 3));
    assertThat(shiftedRangeCtx1_1).hasRange(3, 0, 3, 15);
    TextRange shiftedRangeCtx1_2 = shifter.computeShiftedLocation(ctx, range(2, 1, 2, 3));
    assertThat(shiftedRangeCtx1_2).hasRange(3, 0, 3, 15);

    TextRange shiftedRangeCtx2_1 = shifter.computeShiftedLocation(differentCtx, range(1, 1, 1, 3));
    assertThat(shiftedRangeCtx2_1).hasRange(4, 0, 4, 21);
    TextRange shiftedRangeCtx2_2 = shifter.computeShiftedLocation(differentCtx, range(2, 1, 2, 3));
    assertThat(shiftedRangeCtx2_2).hasRange(4, 0, 4, 21);
  }

  @Test
  void shouldShiftLocationsWithoutExplicitNumbers() {
    setLinesSizes(ctx, 2, 3, 4, 5, 10);
    shifter.addShiftedLine(ctx, 4, 1);
    shifter.addShiftedLine(ctx, 5, 2);

    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, range(3, 1, 3, 3));

    assertThat(shiftedRange).hasRange(1, 0, 1, 2);
  }

  @Test
  void shouldShiftLocationsWithoutExplicitNumbersWithRanges() {
    setLinesSizes(ctx, 2, 3, 4, 5, 10);
    shifter.addShiftedLine(ctx, 4, 1, 3);
    shifter.addShiftedLine(ctx, 5, 4);

    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, range(3, 1, 3, 3));

    assertThat(shiftedRange).hasRange(1, 0, 3, 4);
  }

  @Test
  void shouldShiftSecondaryLocationOnUnsetFilePath() {
    setLinesSizes(ctx, 5, 10);
    shifter.addShiftedLine(ctx, 1, 2);

    var secondaryLocation = new SecondaryLocation(range(1, 1, 1, 3), "message");
    SecondaryLocation shiftedSecondaryLocation = shifter.computeShiftedSecondaryLocation(ctx, secondaryLocation);

    assertThat(shiftedSecondaryLocation.textRange).hasRange(2, 0, 2, 10);
    Assertions.assertThat(shiftedSecondaryLocation.message).isEqualTo(secondaryLocation.message);
    Assertions.assertThat(shiftedSecondaryLocation.filePath).isEqualTo(secondaryLocation.filePath);
  }

  @Test
  void shouldShiftSecondaryLocationOnDefinedFilePathPointingToPrimaryFile() {
    setLinesSizes(ctx, 5, 10);
    shifter.addShiftedLine(ctx, 1, 2);

    var secondaryLocation = new SecondaryLocation(range(1, 1, 1, 3), "message", "primaryFile");
    SecondaryLocation shiftedSecondaryLocation = shifter.computeShiftedSecondaryLocation(ctx, secondaryLocation);

    assertThat(shiftedSecondaryLocation.textRange).hasRange(2, 0, 2, 10);
    Assertions.assertThat(shiftedSecondaryLocation.message).isEqualTo(secondaryLocation.message);
    Assertions.assertThat(shiftedSecondaryLocation.filePath).isEqualTo(secondaryLocation.filePath);
  }

  @Test
  void shouldNotShiftSecondaryLocationOnSecondaryFile() {
    setLinesSizes(ctx, 5, 10);
    shifter.addShiftedLine(ctx, 1, 2);

    InputFile secondaryFile = createInputFile("secondaryFile");
    context.fileSystem().add(secondaryFile);

    var secondaryLocation = new SecondaryLocation(range(1, 1, 1, 3), "message", "secondaryFile");
    SecondaryLocation shiftedSecondaryLocation = shifter.computeShiftedSecondaryLocation(ctx, secondaryLocation);

    Assertions.assertThat(secondaryLocation).isEqualTo(shiftedSecondaryLocation);
  }

  @Test
  void shouldNotShiftSecondaryLocationOnNonExistingFile() {
    setLinesSizes(ctx, 5, 10);
    shifter.addShiftedLine(ctx, 1, 2);

    var secondaryLocation = new SecondaryLocation(range(1, 1, 1, 3), "message", "nonExistingFile");
    SecondaryLocation shiftedSecondaryLocation = shifter.computeShiftedSecondaryLocation(ctx, secondaryLocation);

    Assertions.assertThat(secondaryLocation).isEqualTo(shiftedSecondaryLocation);
  }

  @Test
  void shouldShiftYamlExceptions() {
    setLinesSizes(ctx, 2);
    shifter.addShiftedLine(ctx, 2, 1, 1);
    var exception = new ParserException(null, Optional.of(Mockito.mock(Mark.class)), null,
      Optional.of(new Mark("test", 1, 2, 1, new int[] {1, 1, 1, 1, 1}, 1)));

    var shiftedException = shifter.shiftMarkedYamlException(ctx, exception);

    Assertions.assertThat(shiftedException).isInstanceOf(ShiftedMarkedYamlEngineException.class);
    Assertions.assertThat(shiftedException.getProblemMark()).isPresent()
      .hasValueSatisfying(mark -> {
        Assertions.assertThat(mark.getLine()).isEqualTo(1);
        Assertions.assertThat(mark.getColumn()).isZero();
      });
  }

  @Test
  void shouldKeepYamlExceptionsWithoutMark() {
    setLinesSizes(ctx, 2);
    shifter.addShiftedLine(ctx, 2, 1, 1);
    var exception = new ParserException(null, Optional.of(Mockito.mock(Mark.class)), null, Optional.empty());

    var shiftedException = shifter.shiftMarkedYamlException(ctx, exception);

    Assertions.assertThat(shiftedException).isSameAs(exception);
  }

  @Test
  void shouldComputeHelmValuePathTextRange() {
    var helmInputFileContext = inputFileContextWithTree();
    var textRange = shifter.computeHelmValuePathTextRange(helmInputFileContext, range(1, 6, 1, 22));
    assertThat(textRange).hasRange(1, 15, 1, 18);
  }

  @Test
  void shouldReturnTheSameTextRangeWhenGoTreeIsNull() {
    var helmInputFileContext = inputFileContextWithTree();
    helmInputFileContext.setGoTemplateTree(null);
    var range = range(1, 6, 1, 22);
    var textRange = shifter.computeHelmValuePathTextRange(helmInputFileContext, range);
    assertThat(textRange).isSameAs(range);
  }

  @Test
  void shouldReturnTheSameTextRangeWhenSourceWithCommentsIsNull() {
    var helmInputFileContext = inputFileContextWithTree();
    helmInputFileContext.setSourceWithComments(null);
    var range = range(1, 6, 1, 22);
    var textRange = shifter.computeHelmValuePathTextRange(helmInputFileContext, range);
    assertThat(textRange).isSameAs(range);
  }

  @Test
  void shouldReturnTheSameTextRangeWhenTextRangeDoesntOverlapHelmAst() {
    var helmInputFileContext = inputFileContextWithTree();
    var range = range(1, 0, 1, 3);
    var textRange = shifter.computeHelmValuePathTextRange(helmInputFileContext, range);
    assertThat(textRange).isSameAs(range);
  }

  @Test
  void shouldReturnTheSameTextRangeWhenIoException() throws IOException {
    InputFile inputFileMock = mock(InputFile.class);
    when(inputFileMock.toString()).thenReturn("path/to/file.yaml");
    when(inputFileMock.contents()).thenThrow(new IOException("error"));
    var helmInputFileContext = new HelmInputFileContext(null, inputFileMock);
    helmInputFileContext.setGoTemplateTree(mock(GoTemplateTreeImpl.class));
    helmInputFileContext.setSourceWithComments("dummy");
    var range = range(1, 0, 1, 3);

    var textRange = shifter.computeHelmValuePathTextRange(helmInputFileContext, range);

    assertThat(textRange).isSameAs(range);
    Assertions.assertThat(logTester.logs(Level.DEBUG))
      .contains("Unable to read file path/to/file.yaml raising issue on less precise location");
  }

  void setLinesSizes(InputFileContext ctx, int... linesSizes) {
    for (int lineNumber = 1; lineNumber <= linesSizes.length; lineNumber++) {
      shifter.addLineSize(ctx, lineNumber, linesSizes[lineNumber - 1]);
    }
  }

  private static InputFile createInputFile(String fileName) {
    InputFile inputFile = IacTestUtils.inputFile(fileName, baseDir.toPath(), "", null);
    context.fileSystem().add(inputFile);
    return inputFile;
  }

  private HelmInputFileContext inputFileContextWithTree() {
    var valuesFile = new TestInputFileBuilder("test", ".")
      .setContents("bar: baz")
      .build();
    var inputFileContext = new HelmInputFileContext(mockSensorContextWithEnabledFeature(), inputFile("foo.yaml", Path.of("."), "bar: {{ .Values.bar }}", null));
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));

    var fieldNode = new FieldNodeImpl(15, 3, List.of("Values", "bar"));
    var command = new CommandNodeImpl(8, 11, List.of(fieldNode));
    var pipeNode = new PipeNodeImpl(8, 11, List.of(), List.of(command));
    var actionNode = new ActionNodeImpl(8, 15, pipeNode);
    var textNode = new TextNodeImpl(0, 5, "bar: ");
    ListNodeImpl root = new ListNodeImpl(0, 15, List.of(textNode, actionNode));
    var goTemplateTree = new GoTemplateTreeImpl("test", "test", 0, root);
    inputFileContext.setGoTemplateTree(goTemplateTree);
    inputFileContext.setSourceWithComments("bar: {{ .Values.bar }} #1");
    return inputFileContext;
  }

  private SensorContext mockSensorContextWithEnabledFeature() {
    var config = mock(Configuration.class);
    when(config.getBoolean(AdjustableChecksVisitor.ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY)).thenReturn(Optional.of(true));
    var sensorContext = mock(SensorContext.class);
    when(sensorContext.config()).thenReturn(config);
    return sensorContext;
  }
}
