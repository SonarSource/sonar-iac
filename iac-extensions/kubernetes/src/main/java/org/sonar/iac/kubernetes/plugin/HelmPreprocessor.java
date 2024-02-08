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

import java.nio.file.Path;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import static org.sonar.iac.helm.LineNumberCommentInserter.addLineComments;

public class HelmPreprocessor {
  public static String preProcess(String source, InputFileContext inputFileContext, LocationShifter locationShifter) {
    locationShifter.readLinesSizes(source, inputFileContext);
    return addLineComments(source);
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
