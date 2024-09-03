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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.plugin.filesystem.SonarLintFileSystemProvider;
import org.sonar.iac.kubernetes.plugin.predicates.KubernetesOrHelmFilePredicate;
import org.sonar.iac.kubernetes.visitors.ProjectContext;
import org.sonarsource.api.sonarlint.SonarLintSide;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileEvent;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileListener;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileSystem;

@SonarLintSide(lifespan = "MODULE")
public class SonarLintFileListener implements ModuleFileListener {

  private static final Logger LOG = LoggerFactory.getLogger(SonarLintFileListener.class);

  private final ModuleFileSystem moduleFileSystem;
  private SensorContext sensorContext;
  private KubernetesAnalyzer analyzer;
  private ProjectContext projectContext;
  private SonarLintFileSystemProvider fileSystemProvider;
  private Map<String, String> inputFilesContents = new HashMap<>();

  public SonarLintFileListener(ModuleFileSystem moduleFileSystem) {
    this.moduleFileSystem = moduleFileSystem;
  }

  public void initContext(SensorContext sensorContext, KubernetesAnalyzer analyzer, ProjectContext projectContext, SonarLintFileSystemProvider fileSystemProvider) {
    this.sensorContext = sensorContext;
    this.analyzer = analyzer;
    this.projectContext = projectContext;
    this.fileSystemProvider = fileSystemProvider;
    if (inputFilesContents.isEmpty()) {
      var predicate = new KubernetesOrHelmFilePredicate(sensorContext);
      var inputFiles = moduleFileSystem.files()
        .filter(predicate::apply)
        .toList();

      storeInputFilesContent(inputFiles);
      analyzer.analyseFiles(sensorContext, inputFiles, KubernetesLanguage.KEY);
      LOG.info("Finished building Kubernetes Project Context");
    }
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

    LOG.info("Module file event {} for file {}", moduleFileEvent.getType(), moduleFileEvent.getTarget());
    // the projectContext may be null if SonarLint calls this method before initContext()
    // it happens when starting IDE
    if (projectContext != null) {
      var uri = getPath(moduleFileEvent);
      projectContext.removeResource(uri);
      inputFilesContents.remove(moduleFileEvent.getTarget().filename());
      if (moduleFileEvent.getType() != ModuleFileEvent.Type.DELETED) {
        inputFilesContents.put(moduleFileEvent.getTarget().filename(), content(moduleFileEvent.getTarget()));
        analyzer.analyseFiles(sensorContext, List.of(moduleFileEvent.getTarget()), KubernetesLanguage.KEY);
      }
      LOG.info("Kubernetes Project Context updated");
    } else {
      LOG.info("Kubernetes Project Context not updated");
    }
  }

  public Map<String, String> inputFilesContents() {
    return inputFilesContents;
  }

  private static String getPath(ModuleFileEvent moduleFileEvent) {
    return getPath(moduleFileEvent.getTarget());
  }

  private static String getPath(InputFile inputfile) {
    return Path.of(inputfile.uri()).normalize().toUri().toString();
  }

  private void storeInputFilesContent(List<InputFile> inputFiles) {
    inputFilesContents = inputFiles.stream()
      .collect(Collectors.toMap(SonarLintFileListener::getPath, SonarLintFileListener::content));
  }

  private static String content(InputFile inputFile) {
    try {
      return inputFile.contents();
    } catch (IOException e) {
      throw ParseException.createGeneralParseException("read", inputFile, e, null);
    }
  }
}
