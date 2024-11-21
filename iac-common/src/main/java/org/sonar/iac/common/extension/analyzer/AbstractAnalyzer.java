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
package org.sonar.iac.common.extension.analyzer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

import static org.sonar.iac.common.extension.ExceptionUtils.getStackTrace;

public abstract class AbstractAnalyzer implements Analyzer {

  protected static final long PROGRESS_REPORT_PERIOD_MILLIS = TimeUnit.SECONDS.toMillis(10);
  private static final Logger LOG = LoggerFactory.getLogger(AbstractAnalyzer.class);

  private final String repositoryKey;
  protected final TreeParser<? extends Tree> parser;
  protected final DurationStatistics statistics;

  protected AbstractAnalyzer(String repositoryKey, TreeParser<? extends Tree> parser, DurationStatistics statistics) {
    this.repositoryKey = repositoryKey;
    this.parser = parser;
    this.statistics = statistics;
  }

  protected InputFileContext createInputFileContext(SensorContext sensorContext, InputFile inputFile) {
    return new InputFileContext(sensorContext, inputFile);
  }

  protected static String readContent(InputFileContext inputFileContext) {
    String content;
    try {
      content = inputFileContext.inputFile.contents();
    } catch (IOException | RuntimeException e) {
      throw ParseException.toParseException("read", inputFileContext, e);
    }

    if (content.isBlank()) {
      return null;
    }
    return content;
  }

  public Tree parse(String content, @Nullable InputFileContext inputFileContext) {
    try {
      return parser.parse(content, inputFileContext);
    } catch (ParseException e) {
      throw e;
    } catch (RuntimeException e) {
      throw ParseException.toParseException("parse", inputFileContext, e);
    }
  }

  protected void visit(List<TreeVisitor<InputFileContext>> visitors, InputFileContext inputFileContext, Tree tree) {
    for (TreeVisitor<InputFileContext> visitor : visitors) {
      try {
        String visitorId = visitor.getClass().getSimpleName();
        statistics.time(visitorId, () -> visitor.scan(inputFileContext, tree));
      } catch (RuntimeException e) {
        inputFileContext.reportAnalysisError(e.getMessage(), null);
        LOG.error("Cannot analyse '{}': {}", inputFileContext.inputFile, e.getMessage(), e);
        interruptOnFailFast(inputFileContext.sensorContext, inputFileContext.inputFile, e);
      }
    }
  }

  protected static void interruptOnFailFast(SensorContext context, InputFile inputFile, Exception e) {
    if (IacSensor.isFailFast(context)) {
      throw new IllegalStateException("Exception when analyzing '" + inputFile + "'", e);
    }
  }

  protected void reportParseError(ParseException exception, InputFileContext inputFileContext) {
    logParsingError(exception);
    inputFileContext.reportParseError(repositoryKey, exception.getPosition());
  }

  private static void logParsingError(ParseException e) {
    LOG.error(e.getMessage());
    String detailedMessage = e.getDetails();
    if (detailedMessage != null) {
      LOG.debug(detailedMessage);
    }
    String stackTrace = getStackTrace(e);
    LOG.debug(stackTrace);
  }
}
