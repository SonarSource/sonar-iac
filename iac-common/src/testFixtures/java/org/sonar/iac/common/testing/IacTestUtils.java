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
package org.sonar.iac.common.testing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class IacTestUtils {

  public static final SonarRuntime SONAR_RUNTIME_10_6 = SonarRuntimeImpl.forSonarQube(Version.create(10, 6), SonarQubeSide.SERVER,
    SonarEdition.COMMUNITY);
  public static final SonarRuntime SONAR_RUNTIME_9_9 = SonarRuntimeImpl.forSonarQube(Version.create(9, 9), SonarQubeSide.SERVER,
    SonarEdition.COMMUNITY);

  private IacTestUtils() {
    // utils class
  }

  /**
   * @deprecated Text blocks from Java 15 should be used instead.
   */
  @Deprecated(since = "1.28", forRemoval = true)
  public static String code(String... lines) {
    return StringUtils.join(lines, "\n");
  }

  public static InputFile inputFile(String fileName, String language) {
    return inputFile(fileName, Path.of("src/test/resources"), language);
  }

  public static InputFile inputFile(String fileName, Path baseDir) {
    return inputFile(fileName, baseDir, null);
  }

  public static InputFile inputFile(String fileName, Path baseDir, @Nullable String language) {
    try {
      var content = Files.readString(baseDir.resolve(fileName));
      return inputFile(fileName, baseDir, content, language);
    } catch (IOException e) {
      throw new IllegalStateException("File not found", e);
    }
  }

  public static InputFile inputFile(String fileName, Path baseDir, String content, @Nullable String language) {
    return new TestInputFileBuilder("moduleKey", fileName)
      .setModuleBaseDir(baseDir)
      .setType(InputFile.Type.MAIN)
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(language)
      .setContents(content)
      .build();
  }

  public static InputFile invalidInputFile() throws IOException {
    InputFile file = mock(InputFile.class);
    when(file.contents()).thenThrow(new IOException("Invalid file mock"));
    when(file.toString()).thenReturn("InvalidFile");
    return file;
  }

  public static void addFileToSensorContext(SensorContextTester context, Path baseDir, String fileName) {
    var inputFile = inputFile(fileName, baseDir);
    context.fileSystem().add(inputFile);
  }

  public static InputFileContext createInputFileContextMock(String filename) {
    return createInputFileContextMock(filename, "");
  }

  public static InputFileContext createInputFileContextMock(String filename, String languageKey) {
    var inputFile = mock(InputFile.class);
    var inputFileContext = new InputFileContext(mock(SensorContext.class), inputFile);
    when(inputFile.toString()).thenReturn("dir1/dir2/" + filename);
    when(inputFile.filename()).thenReturn(filename);
    when(inputFile.language()).thenReturn(languageKey);
    return inputFileContext;
  }
}
