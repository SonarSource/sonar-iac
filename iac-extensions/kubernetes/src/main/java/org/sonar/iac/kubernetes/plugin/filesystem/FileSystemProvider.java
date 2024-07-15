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
package org.sonar.iac.kubernetes.plugin.filesystem;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

public interface FileSystemProvider {
  List<String> LINE_SEPARATORS = List.of("\n", "\r\n", "\r", "\u2028", "\u2029");

  Map<String, String> inputFilesForHelm(HelmInputFileContext inputFileContext);

  static String normalizeToUnixPathSeparator(String filename) {
    return filename.replace('\\', '/');
  }

  static boolean containsLineBreak(String filename) {
    return LINE_SEPARATORS.stream().anyMatch(filename::contains);
  }

  Path getBasePath();
}
