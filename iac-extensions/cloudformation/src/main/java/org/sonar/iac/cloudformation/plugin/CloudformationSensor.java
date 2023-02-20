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
package org.sonar.iac.cloudformation.plugin;

import java.io.BufferedInputStream;
import java.io.IOException;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.notifications.AnalysisWarnings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.cloudformation.checks.CloudformationCheckList;
import org.sonar.iac.cloudformation.parser.CloudformationConverter;
import org.sonar.iac.cloudformation.reports.CfnLintImporter;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.YamlSensor;
import org.sonarsource.analyzer.commons.ExternalReportProvider;

public class CloudformationSensor extends YamlSensor {

  private static final int DEFAULT_BUFFER_SIZE = 8192;

  private final AnalysisWarnings analysisWarnings;

  public CloudformationSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
                              NoSonarFilter noSonarFilter, CloudformationLanguage language, AnalysisWarnings analysisWarnings) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, CloudformationCheckList.checks());
    this.analysisWarnings = analysisWarnings;
  }

  @Override
  protected TreeParser<Tree> treeParser() {
    return new YamlParser(new CloudformationConverter());
  }

  @Override
  protected FilePredicate customFilePredicate(SensorContext sensorContext) {
    return new FileIdentificationPredicate(sensorContext.config().get(CloudformationSettings.FILE_IDENTIFIER_KEY).orElse(""));
  }

  @Override
  protected String repositoryKey() {
    return CloudformationExtension.REPOSITORY_KEY;
  }

  @Override
  protected void importExternalReports(SensorContext sensorContext) {
    ExternalReportProvider.getReportFiles(sensorContext, CloudformationSettings.CFN_LINT_REPORTS_KEY)
      .forEach(report -> CfnLintImporter.importReport(sensorContext, report, analysisWarnings));
  }

  @Override
  protected String getActivationSettingKey() {
    return CloudformationSettings.ACTIVATION_KEY;
  }


  private static class FileIdentificationPredicate implements FilePredicate {
    private static final Logger LOG = Loggers.get(FileIdentificationPredicate.class);
    private final String fileIdentifier;

    public FileIdentificationPredicate(String fileIdentifier) {
      this.fileIdentifier = fileIdentifier;
    }

    @Override
    public boolean apply(InputFile inputFile) {
      return hasFileIdentifier(inputFile);
    }

    private boolean hasFileIdentifier(InputFile inputFile) {
      if ("".equals(fileIdentifier)) {
        return true;
      }

      try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputFile.inputStream())) {
        // Only firs 8k bytes is read to avoid slow execution for big one-line files
        byte[] bytes = bufferedInputStream.readNBytes(DEFAULT_BUFFER_SIZE);
        String text = new String(bytes, inputFile.charset());
        String[] lines = text.split("\n");
        for (String line : lines) {
          if (line.contains(fileIdentifier)) {
            return true;
          }
        }
      } catch (IOException e) {
        LOG.error(String.format("Unable to read file: %s.", inputFile.uri()));
        LOG.error(e.getMessage());
      }

      return false;
    }
  }
}
