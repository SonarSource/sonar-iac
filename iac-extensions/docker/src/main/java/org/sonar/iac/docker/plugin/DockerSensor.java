/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.docker.plugin;

import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.docker.checks.DockerCheckList;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.visitors.DockerHighlightingVisitor;
import org.sonar.iac.docker.visitors.DockerMetricsVisitor;
import org.sonar.iac.docker.visitors.DockerSymbolVisitor;

@Phase(name = Phase.Name.POST)
public class DockerSensor extends IacSensor {
  private final Checks<IacCheck> checks;

  public DockerSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
                         NoSonarFilter noSonarFilter, DockerLanguage language) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language);
    checks = checkFactory.create(DockerExtension.REPOSITORY_KEY);
    checks.addAnnotatedChecks(DockerCheckList.checks());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .processesFilesIndependently()
      .name("IaC " + language.getName() + " Sensor");
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    FileSystem fileSystem = sensorContext.fileSystem();
    FilePredicates p = fileSystem.predicates();
    return p.and(
      p.or(
        p.matchesPathPattern("**/Dockerfile"),
        p.matchesPathPattern("**/Dockerfile.*"),
        p.matchesPathPattern("**/**.Dockerfile"),
        p.matchesPathPattern("**/**.dockerfile")
      ),
      p.hasType(InputFile.Type.MAIN));
  }

  @Override
  protected DockerParser treeParser() {
    return DockerParser.create();
  }

  @Override
  protected String repositoryKey() {
    return DockerExtension.REPOSITORY_KEY;
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    return List.of(
      new DockerSymbolVisitor(),
      new ChecksVisitor(checks, statistics),
      new DockerMetricsVisitor(fileLinesContextFactory, noSonarFilter),
      new DockerHighlightingVisitor()
    );
  }

  @Override
  protected String getActivationSettingKey() {
    return DockerSettings.ACTIVATION_KEY;
  }
}
