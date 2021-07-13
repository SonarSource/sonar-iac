/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
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

  private final Checks<IacCheck> checks;

  public CloudformationSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter, CloudformationLanguage language) {
    super(fileLinesContextFactory, noSonarFilter, language);
    checks = checkFactory.create(CloudformationExtension.REPOSITORY_KEY);
    checks.addAnnotatedChecks((Iterable<?>) CloudformationCheckList.checks());
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
    return sensorContext.fileSystem().predicates().and(super.mainFilePredicate(sensorContext),
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
