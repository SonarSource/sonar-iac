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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.tree.api.GoTemplateTree;

public class HelmInputFileContext extends InputFileContext {
  @Nullable
  private GoTemplateTree goTemplateTree;
  private Map<String, InputFile> additionalFiles = new HashMap<>();

  public HelmInputFileContext(SensorContext sensorContext, InputFile inputFile) {
    super(sensorContext, inputFile);
  }

  public void setAdditionalFiles(Map<String, InputFile> additionalFiles) {
    this.additionalFiles = Collections.unmodifiableMap(additionalFiles);
  }

  public boolean hasAdditionalFile(String filePath) {
    return additionalFiles.containsKey(filePath);
  }

  @CheckForNull
  public InputFile getValuesFile() {
    return additionalFiles.get("values.yaml");
  }

  public Map<String, InputFile> getAdditionalFiles() {
    return Collections.unmodifiableMap(additionalFiles);
  }

  @CheckForNull
  public GoTemplateTree getGoTemplateTree() {
    return goTemplateTree;
  }

  public void setGoTemplateTree(@Nullable GoTemplateTree goTemplateTree) {
    this.goTemplateTree = goTemplateTree;
  }
}
