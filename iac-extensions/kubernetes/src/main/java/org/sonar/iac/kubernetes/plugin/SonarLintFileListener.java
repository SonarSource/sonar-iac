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
package org.sonar.iac.kubernetes.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.analyzer.Analyzer;
import org.sonar.iac.common.predicates.KubernetesOrHelmFilePredicate;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.visitors.ProjectContextEnricherVisitor;
import org.sonar.iac.kubernetes.visitors.ProjectContextImpl;
import org.sonarsource.api.sonarlint.SonarLintSide;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileEvent;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileListener;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileSystem;

import static org.sonar.iac.kubernetes.plugin.KubernetesAnalyzerFactory.createAnalyzerForUpdatingProjectContext;

@SonarLintSide(lifespan = "MODULE")
public class SonarLintFileListener implements ModuleFileListener {

  private static final Logger LOG = LoggerFactory.getLogger(SonarLintFileListener.class);

  private final ModuleFileSystem moduleFileSystem;
  private final ProjectContextImpl projectContext = new ProjectContextImpl();
  private SensorContext sensorContext;
  private Analyzer analyzer;
  private Map<String, String> inputFilesContents = new HashMap<>();
  private boolean initialized = false;

  public SonarLintFileListener(ModuleFileSystem moduleFileSystem) {
    this.moduleFileSystem = moduleFileSystem;
  }

  public void initContext(SensorContext sensorContext, @Nullable HelmProcessor helmProcessor) {
    this.sensorContext = sensorContext;
    var statistics = new DurationStatistics(sensorContext.config());
    analyzer = createAnalyzerForUpdatingProjectContext(List.of(new ProjectContextEnricherVisitor(projectContext)), statistics, helmProcessor, this);

    if (!initialized) {
      // The analysis is executed for the first time by SonarLint, the content of all relevant files has to be stored in inputFilesContents
      var predicate = new KubernetesOrHelmFilePredicate(sensorContext, true, statistics.timer("SonarLintFileListener.KubernetesOrHelmFilePredicate"));
      var inputFiles = moduleFileSystem.files()
        .filter(predicate::apply)
        .toList();

      inputFilesContents = inputFiles.stream()
        .collect(Collectors.toMap(SonarLintFileListener::getPath, SonarLintFileListener::content));
      updateProjectContext(sensorContext, inputFiles);
      LOG.info("Finished building Kubernetes Project Context");
    }
    initialized = true;
  }

  private void updateProjectContext(SensorContext sensorContext, List<InputFile> inputFiles) {
    // it will fill the projectContext with the data needed for cross-file analysis
    analyzer.analyseFiles(sensorContext, inputFiles, KubernetesLanguage.KEY);
  }

  @Override
  public void process(ModuleFileEvent moduleFileEvent) {
    InputFile target = moduleFileEvent.getTarget();
    String language = target.language();
    if (language == null || !HelmFileSystem.INCLUDED_EXTENSIONS.contains(language)) {
      LOG.info("Module file event for {} for file {} has been ignored because it's not a Kubernetes file.",
        moduleFileEvent.getType(), moduleFileEvent.getTarget());
      return;
    }

    // The method process(ModuleFileEvent) may be called before initContext().
    // In that case modifying projectContext should be skipped
    // It happens when starting IDE
    if (!initialized) {
      LOG.info("Module file event {} for file {}, ignored as context was not initialized", moduleFileEvent.getType(), moduleFileEvent.getTarget());
      return;
    }

    LOG.info("Module file event {} for file {}", moduleFileEvent.getType(), moduleFileEvent.getTarget());
    var uri = getPath(moduleFileEvent);
    projectContext.removeResource(uri);
    inputFilesContents.remove(uri);
    if (moduleFileEvent.getType() != ModuleFileEvent.Type.DELETED) {
      inputFilesContents.put(uri, content(moduleFileEvent.getTarget()));
      updateProjectContext(sensorContext, List.of(moduleFileEvent.getTarget()));
    }
    LOG.info("Kubernetes Project Context updated");
  }

  public Map<String, String> inputFilesContents() {
    return inputFilesContents;
  }

  public ProjectContextImpl getProjectContext() {
    return projectContext;
  }

  private static String getPath(ModuleFileEvent moduleFileEvent) {
    return getPath(moduleFileEvent.getTarget());
  }

  private static String getPath(InputFile inputfile) {
    return Path.of(inputfile.uri()).normalize().toUri().toString();
  }

  private static String content(InputFile inputFile) {
    try {
      return inputFile.contents();
    } catch (IOException e) {
      throw ParseException.createGeneralParseException("read", inputFile, e, null);
    }
  }
}
