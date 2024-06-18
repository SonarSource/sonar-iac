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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.ProgressReport;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.sonar.iac.common.extension.ExceptionUtils.getStackTrace;

public class Analyzer {

  private static final Logger LOG = LoggerFactory.getLogger(Analyzer.class);

  private final String repositoryKey;
  protected final TreeParser<? extends Tree> parser;
  private final List<TreeVisitor<InputFileContext>> visitors;
  private final DurationStatistics statistics;

  public Analyzer(String repositoryKey, TreeParser<? extends Tree> parser, List<TreeVisitor<InputFileContext>> visitors, DurationStatistics statistics) {
    this.repositoryKey = repositoryKey;
    this.parser = parser;
    this.visitors = Collections.unmodifiableList(visitors);
    this.statistics = statistics;
  }

  public boolean analyseFiles(SensorContext sensorContext, Iterable<InputFile> inputFiles, ProgressReport progressReport) {
    for (InputFile inputFile : inputFiles) {
      if (sensorContext.isCancelled()) {
        return false;
      }
      var inputFileContext = createInputFileContext(sensorContext, inputFile);
      try {
        analyseFile(sensorContext, inputFileContext);
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

  private void analyseFile(SensorContext sensorContext, InputFileContext inputFileContext) {
    var content = readContent(inputFileContext);
    if (content == null) {
      return;
    }

    Tree tree = statistics.time("Parse", () -> parse(inputFileContext, content));

    visit(inputFileContext, tree);
  }

  private static String readContent(InputFileContext inputFileContext) {
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

  public Tree parse(@Nullable InputFileContext inputFileContext, String content) {
    try {
      return parser.parse(content, inputFileContext);
    } catch (ParseException e) {
      throw e;
    } catch (RuntimeException e) {
      throw ParseException.toParseException("parse", inputFileContext, e);
    }
  }

  public void visit(InputFileContext inputFileContext, Tree tree) {
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
