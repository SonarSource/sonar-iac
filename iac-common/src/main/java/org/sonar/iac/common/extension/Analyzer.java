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
package org.sonar.iac.common.extension;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.ProgressReport;

import static org.sonar.iac.common.extension.ExceptionUtils.getStackTrace;

public class Analyzer {

  private static final Logger LOG = LoggerFactory.getLogger(Analyzer.class);

  private final String repositoryKey;
  private final TreeParser<? extends Tree> parser;
  private final List<TreeVisitor<InputFileContext>> visitors;
  private final DurationStatistics statistics;

  public Analyzer(String repositoryKey, TreeParser<? extends Tree> parser, List<TreeVisitor<InputFileContext>> visitors, DurationStatistics statistics) {
    this.repositoryKey = repositoryKey;
    this.parser = parser;
    this.visitors = visitors;
    this.statistics = statistics;
  }

  boolean analyseFiles(SensorContext sensorContext, List<InputFile> inputFiles, ProgressReport progressReport) {
    for (InputFile inputFile : inputFiles) {
      if (sensorContext.isCancelled()) {
        return false;
      }
      var inputFileContext = createInputFileContext(sensorContext, inputFile);
      try {
        analyseFile(inputFileContext);
      } catch (ParseException e) {
        logParsingError(e);
        inputFileContext.reportParseError(repositoryKey, e.getPosition());
      }
      progressReport.nextFile();
    }
    return true;
  }

  protected InputFileContext createInputFileContext(SensorContext sensorContext, InputFile inputFile) {
    return new InputFileContext(sensorContext, inputFile);
  }

  private void analyseFile(InputFileContext inputFileContext) {
    var inputFile = inputFileContext.inputFile;
    String content;
    try {
      content = inputFile.contents();
    } catch (IOException | RuntimeException e) {
      throw ParseException.toParseException("read", inputFileContext, e);
    }

    if (content.isBlank()) {
      return;
    }

    Tree tree = statistics.time("Parse", () -> {
      try {
        return parser.parse(content, inputFileContext);
      } catch (ParseException e) {
        throw e;
      } catch (RuntimeException e) {
        throw ParseException.toParseException("parse", inputFileContext, e);
      }
    });

    for (TreeVisitor<InputFileContext> visitor : visitors) {
      try {
        String visitorId = visitor.getClass().getSimpleName();
        statistics.time(visitorId, () -> visitor.scan(inputFileContext, tree));
      } catch (RuntimeException e) {
        inputFileContext.reportAnalysisError(e.getMessage(), null);
        LOG.error("Cannot analyse '{}': {}", inputFile, e.getMessage(), e);

        interruptOnFailFast(inputFileContext.sensorContext, inputFile, e);
      }
    }
  }

  private static void interruptOnFailFast(SensorContext context, InputFile inputFile, Exception e) {
    if (IacSensor.isFailFast(context)) {
      throw new IllegalStateException("Exception when analyzing '" + inputFile + "'", e);
    }
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
