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
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.TextRangeAssert.assertThat;

final class LocationShifterTest {

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
}
