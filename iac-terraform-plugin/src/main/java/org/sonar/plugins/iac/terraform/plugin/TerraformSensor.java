/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.plugin;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.plugins.iac.terraform.parser.HclParser;
import org.sonarsource.analyzer.commons.ProgressReport;

public class TerraformSensor implements Sensor {

  // TODO use for Metrics
  private final FileLinesContextFactory fileLinesContextFactory;
  // TODO use for Metrics
  private final NoSonarFilter noSonarFilter;

  public TerraformSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    checkFactory.create(TerraformPlugin.REPOSITORY_KEY);
  }

  @Override
  public void describe(SensorDescriptor sensorDescriptor) {
    sensorDescriptor.onlyOnLanguage(TerraformPlugin.LANGUAGE_KEY)
      .name("IaC Terraform Sensor");
  }

  @Override
  public void execute(SensorContext sensorContext) {
    FileSystem fileSystem = sensorContext.fileSystem();
    FilePredicate mainFilePredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasLanguage(TerraformPlugin.LANGUAGE_KEY),
      fileSystem.predicates().hasType(InputFile.Type.MAIN));
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(mainFilePredicate);
    List<String> filenames = StreamSupport.stream(inputFiles.spliterator(), false).map(InputFile::toString).collect(Collectors.toList());
    ProgressReport progressReport = new ProgressReport("Progress of the " + TerraformPlugin.LANGUAGE_NAME + " analysis", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(filenames);
    boolean success = false;
    Analyzer analyzer = Analyzer.create(new HclParser());
    try {
      success = analyzer.analyseFiles(sensorContext, inputFiles, progressReport);
    } finally {
      if (success) {
        progressReport.stop();
      } else {
        progressReport.cancel();
      }
    }
  }
}
