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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;

public class HelmPreprocessor {
  public static Map<String, String> preProcess(InputFileContext inputFileContext, Map<String, InputFile> additionalFiles) {
    return validateAndReadFiles(inputFileContext.inputFile, additionalFiles);
  }

  private static Map<String, String> validateAndReadFiles(InputFile inputFile, Map<String, InputFile> files) {
    // Currently we are only looking for the default location of the values file
    if (!files.containsKey("values.yaml") && !files.containsKey("values.yml")) {
      throw parseExceptionFor(inputFile, "Failed to find values file", null);
    }

    Map<String, String> fileContents = new HashMap<>(files.size());

    for (Map.Entry<String, InputFile> filenameToInputFile : files.entrySet()) {
      var additionalInputFile = filenameToInputFile.getValue();
      String fileContent;
      try {
        fileContent = additionalInputFile.contents();
      } catch (IOException e) {
        throw parseExceptionFor(inputFile, "Failed to read file at " + additionalInputFile, e.getMessage());
      }

      fileContents.put(filenameToInputFile.getKey(), fileContent);
    }
    return fileContents;
  }

  private static ParseException parseExceptionFor(InputFile inputFile, String cause, @Nullable String details) {
    return new ParseException("Failed pre-processing of Helm file " + inputFile + ": " + cause, null, details);
  }

  public static String getFileRelativePath(InputFileContext inputFileContext) {
    var filePath = Path.of(inputFileContext.inputFile.uri());
    var chartRootDirectory = HelmFilesystemUtils.retrieveHelmProjectFolder(filePath, inputFileContext.sensorContext.fileSystem().baseDir());
    String fileRelativePath;
    if (chartRootDirectory == null) {
      fileRelativePath = inputFileContext.inputFile.filename();
    } else {
      fileRelativePath = chartRootDirectory.relativize(filePath).normalize().toString();
      // transform windows to unix path
      fileRelativePath = HelmFilesystemUtils.normalizeToUnixPathSeparator(fileRelativePath);
    }
    return fileRelativePath;
  }
}
