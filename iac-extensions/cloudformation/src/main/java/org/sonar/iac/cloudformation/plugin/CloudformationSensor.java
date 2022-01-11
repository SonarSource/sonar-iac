/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.cloudformation.checks.CloudformationCheckList;
import org.sonar.iac.cloudformation.parser.CloudformationParser;
import org.sonar.iac.cloudformation.reports.CfnLintImporter;
import org.sonar.iac.cloudformation.visitors.CloudformationHighlightingVisitor;
import org.sonar.iac.cloudformation.visitors.CloudformationMetricsVisitor;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.ExternalReportProvider;

public class CloudformationSensor extends IacSensor {
  private static final String JSON_LANGUAGE_KEY = "json";
  private static final String YAML_LANGUAGE_KEY = "yaml";
  private final Checks<IacCheck> checks;

  public CloudformationSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
                              NoSonarFilter noSonarFilter, CloudformationLanguage language) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language);
    checks = checkFactory.create(CloudformationExtension.REPOSITORY_KEY);
    checks.addAnnotatedChecks((Iterable<?>) CloudformationCheckList.checks());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(JSON_LANGUAGE_KEY, YAML_LANGUAGE_KEY)
      .name("IaC " + language.getName() + " Sensor");
  }

  @Override
  protected TreeParser<Tree> treeParser() {
    return new CloudformationParser();
  }

  @Override
  protected String repositoryKey() {
    return CloudformationExtension.REPOSITORY_KEY;
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (isSonarLintContext(sensorContext)) {
      visitors.add(new CloudformationHighlightingVisitor());
      visitors.add(new CloudformationMetricsVisitor(fileLinesContextFactory, noSonarFilter));
    }
    visitors.add(new ChecksVisitor(checks, statistics));
    return visitors;
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    FileSystem fileSystem = sensorContext.fileSystem();
    return fileSystem.predicates().and(fileSystem.predicates().and(
      fileSystem.predicates().or(fileSystem.predicates().hasLanguage(JSON_LANGUAGE_KEY), fileSystem.predicates().hasLanguage(YAML_LANGUAGE_KEY)),
      fileSystem.predicates().hasType(InputFile.Type.MAIN)),
      new FileIdentificationPredicate(sensorContext.config().get(CloudformationSettings.FILE_IDENTIFIER_KEY).orElse("")));
  }

  @Override
  protected void importExternalReports(SensorContext sensorContext) {
    ExternalReportProvider.getReportFiles(sensorContext, CloudformationSettings.CFN_LINT_REPORTS_KEY)
      .forEach(report -> CfnLintImporter.importReport(sensorContext, report));
  }

  @Override
  protected String getActivationSettingKey() {
    return CloudformationSettings.ACTIVATION_KEY;
  }

  @Override
  protected ParseException toParseException(String action, InputFile inputFile, Exception cause) {
    if (!(cause instanceof MarkedYamlEngineException)) {
      return super.toParseException(action, inputFile, cause);
    }

    Optional<Mark> problemMark = ((MarkedYamlEngineException) cause).getProblemMark();
    TextPointer position = null;
    if (problemMark.isPresent()) {
      position = inputFile.newPointer(problemMark.get().getLine() + 1, 0);
    }
    return new ParseException("Cannot " + action + " '" + inputFile + "': " + cause.getMessage(), position);
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

      try (Scanner scanner = new Scanner(inputFile.inputStream(), inputFile.charset().name())) {
        while (scanner.hasNextLine()) {
          if (scanner.nextLine().contains(fileIdentifier)) {
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
