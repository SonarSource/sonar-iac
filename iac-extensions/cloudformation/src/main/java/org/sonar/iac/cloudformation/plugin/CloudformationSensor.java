/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.cloudformation.checks.CloudformationCheckList;
import org.sonar.iac.cloudformation.parser.CloudformationParser;
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
}
