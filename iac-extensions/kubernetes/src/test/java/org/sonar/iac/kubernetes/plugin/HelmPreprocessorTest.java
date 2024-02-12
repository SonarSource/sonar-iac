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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.predicates.DefaultFilePredicates;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class HelmPreprocessorTest {
  private final InputFile inputFile = mock(InputFile.class);
  private final SensorContext sensorContext = mock(SensorContext.class);
  private final InputFileContext inputFileContext = new InputFileContext(sensorContext, inputFile);

  @Test
  void testPreProcess() {
    var source = code(
      "key: |",
      "  .",
      "  .",
      "  .",
      "  .",
      "invalid-key");
    var expected = code(
      "key: | #1",
      "  . #2",
      "  . #3",
      "  . #4",
      "  . #5",
      "invalid-key #6");
    LocationShifter locationShifter = Mockito.mock(LocationShifter.class);

    String result = HelmPreprocessor.preProcess(source, inputFileContext, locationShifter);

    verify(locationShifter).readLinesSizes(source, inputFileContext);
    assertEquals(expected, result);
  }

  @Test
  void testGetFileRelativePath() {
    var fileSystem = mock(FileSystem.class);
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    when(fileSystem.predicates()).thenReturn(new DefaultFilePredicates(Path.of(".")));

    Mockito.when(inputFileContext.inputFile.filename()).thenReturn("testfile.yaml");
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("path/to/file.yaml");

      String result = HelmPreprocessor.getFileRelativePath(inputFileContext);

      assertEquals("testfile.yaml", result);
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
