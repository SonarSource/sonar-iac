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

import java.util.HashMap;
import java.util.Map;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import static java.util.stream.Collectors.toMap;

public class SonarLintFileSystemProvider implements FileSystemProvider {

  private Map<String, String> inputFilesContents = new HashMap<>();

  @Override
  public Map<String, String> inputFilesForHelm(HelmInputFileContext inputFileContext) {
    var helmProjectDirectory = inputFileContext.getHelmProjectDirectory().toUri().toString();
    return inputFilesContents.entrySet().stream()
      .filter(entry -> entry.getKey().startsWith(helmProjectDirectory))
      .filter(entry -> !entry.getKey().equals(inputFileContext.inputFile.uri().toString()))
      .collect(toMap(entry -> entry.getKey().substring(helmProjectDirectory.length()), Map.Entry::getValue));
  }

  public void setInputFilesContents(Map<String, String> inputFilesContents) {
    this.inputFilesContents = inputFilesContents;
  }

  public Map<String, String> getInputFilesContents() {
    return inputFilesContents;
  }
}
