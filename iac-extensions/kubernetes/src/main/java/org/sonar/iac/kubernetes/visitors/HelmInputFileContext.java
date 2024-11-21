/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.visitors;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.filesystem.FileSystemUtils;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.kubernetes.plugin.SonarLintFileListener;
import org.sonar.iac.kubernetes.visitors.LocationShifter.LinesShifting;

public class HelmInputFileContext extends InputFileContext {
  private static final String VALUES_YAML = "values.yaml";
  @Nullable
  private final Path helmProjectDirectory;
  @Nullable
  private GoTemplateTree goTemplateTree;
  private Map<String, String> additionalFiles = new HashMap<>();
  @Nullable
  private String sourceWithComments;

  private final LinesShifting linesShifting = new LinesShifting();

  public HelmInputFileContext(SensorContext sensorContext, InputFile inputFile, @Nullable SonarLintFileListener sonarLintFileListener) {
    super(sensorContext, inputFile);
    if (sonarLintFileListener == null) {
      this.helmProjectDirectory = FileSystemUtils.retrieveHelmProjectFolder(Path.of(inputFile.uri()), sensorContext.fileSystem());
    } else {
      this.helmProjectDirectory = HelmFileSystem.retrieveHelmProjectFolder(Path.of(inputFile.uri()), sensorContext.fileSystem(), sonarLintFileListener);
    }
  }

  public void setAdditionalFiles(Map<String, String> additionalFiles) {
    this.additionalFiles = additionalFiles;
  }

  @CheckForNull
  public String getValuesFile() {
    return additionalFiles.get(VALUES_YAML);
  }

  @CheckForNull
  public String getValuesFilePath() {
    if (additionalFiles.containsKey(VALUES_YAML)) {
      return VALUES_YAML;
    }
    return null;
  }

  public Map<String, String> getAdditionalFiles() {
    return additionalFiles;
  }

  @CheckForNull
  public GoTemplateTree getGoTemplateTree() {
    return goTemplateTree;
  }

  public void setGoTemplateTree(@Nullable GoTemplateTree goTemplateTree) {
    this.goTemplateTree = goTemplateTree;
  }

  public void setSourceWithComments(@Nullable String sourceWithComments) {
    this.sourceWithComments = sourceWithComments;
  }

  @CheckForNull
  public String getSourceWithComments() {
    return sourceWithComments;
  }

  public boolean isInChartRootDirectory() {
    return inputFile.path().getParent() != null && inputFile.path().getParent().equals(helmProjectDirectory);
  }

  public LinesShifting sourceMap() {
    return linesShifting;
  }

  @CheckForNull
  public Path getHelmProjectDirectory() {
    return helmProjectDirectory;
  }

}
