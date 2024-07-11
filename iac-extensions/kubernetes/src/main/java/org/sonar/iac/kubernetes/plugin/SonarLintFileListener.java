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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
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

  public SonarLintFileListener(ModuleFileSystem moduleFileSystem) {
    this.moduleFileSystem = moduleFileSystem;
  }

  public void initContext(SensorContext sensorContext, KubernetesAnalyzer analyzer, ProjectContext projectContext) {
    this.sensorContext = sensorContext;
    this.analyzer = analyzer;
    this.projectContext = projectContext;
    var predicate = new KubernetesOrHelmFilePredicate(sensorContext);
    var inputFiles = moduleFileSystem.files("yaml", InputFile.Type.MAIN)
      .filter(predicate::apply)
      .toList();

    analyzer.analyseFiles(sensorContext, inputFiles, KubernetesLanguage.KEY);
    LOG.info("Finished building Kubernetes Project Context");
  }

  @Override
  public void process(ModuleFileEvent moduleFileEvent) {
    var uri = Path.of(moduleFileEvent.getTarget().uri()).normalize().toUri().toString();
    projectContext.removeResource(uri);
    if (moduleFileEvent.getType() != ModuleFileEvent.Type.DELETED) {
      analyzer.analyseFiles(sensorContext, List.of(moduleFileEvent.getTarget()), KubernetesLanguage.KEY);
    }
  }
}
