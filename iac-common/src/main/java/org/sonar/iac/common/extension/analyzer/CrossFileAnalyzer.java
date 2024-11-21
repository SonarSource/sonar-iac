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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.ProgressReport;

public class CrossFileAnalyzer extends AbstractAnalyzer {

  private final TreeVisitor<InputFileContext> checksVisitor;
  private final List<TreeVisitor<InputFileContext>> visitors;

  /**
   * This version of the analyzer is allowing to perform cross-file analysis.
   * It will apply the {@code parser.parse(...)} method to all files, then apply the {@code visitors} to each of them, and finally apply the provided {@code checksVisitor}.
   * This implies to not provide the checks visitor in the {@code visitors} parameters but separately as the last parameter.
   */
  public CrossFileAnalyzer(String repositoryKey, TreeParser<? extends Tree> parser, List<TreeVisitor<InputFileContext>> visitors, TreeVisitor<InputFileContext> checksVisitor,
    DurationStatistics statistics) {
    super(repositoryKey, parser, statistics);
    this.visitors = visitors;
    this.checksVisitor = checksVisitor;
  }

  public boolean analyseFiles(SensorContext sensorContext, Collection<InputFile> inputFiles, String languageName) {
    List<String> filenames = inputFiles.stream().map(InputFile::toString).toList();
    var progressReportParser = new ProgressReport(progressReportThreadName(languageName, " parsing"), PROGRESS_REPORT_PERIOD_MILLIS, "parsed");
    progressReportParser.start(filenames);

    List<InputFileContext> inputFileContextList = inputFiles.stream()
      .map(inputFile -> createInputFileContext(sensorContext, inputFile))
      .toList();

    // Parse files
    List<FileWithAst> filesWithAst = new ArrayList<>();
    for (InputFileContext inputFileContext : inputFileContextList) {
      if (sensorContext.isCancelled()) {
        progressReportParser.cancel();
        return false;
      }

      try {
        var content = readContent(inputFileContext);
        if (content != null) {
          Tree tree = statistics.time("Parse", () -> parse(content, inputFileContext));
          filesWithAst.add(new FileWithAst(inputFileContext, tree));
        }
      } catch (ParseException e) {
        reportParseError(e, inputFileContext);
      }

      progressReportParser.nextFile();
    }
    progressReportParser.stop();

    var progressReportVisitors = new ProgressReport(progressReportThreadName(languageName, " analysis"),
      PROGRESS_REPORT_PERIOD_MILLIS, "analyzed");
    progressReportVisitors.start(filenames);
    if (!applyVisitors(sensorContext, filesWithAst, visitors, progressReportVisitors)) {
      return false;
    }

    var progressReportCheckVisitor = new ProgressReport(progressReportThreadName(languageName, " analysis"),
      PROGRESS_REPORT_PERIOD_MILLIS, "checked");
    progressReportCheckVisitor.start(filenames);
    return applyVisitors(sensorContext, filesWithAst, List.of(checksVisitor), progressReportCheckVisitor);
  }

  private boolean applyVisitors(SensorContext sensorContext, List<FileWithAst> filesWithAsts, List<TreeVisitor<InputFileContext>> visitorsToBeApplied,
    ProgressReport progressReport) {
    // Visit files
    for (FileWithAst fileWithAsts : filesWithAsts) {
      if (sensorContext.isCancelled()) {
        progressReport.cancel();
        return false;
      }

      visit(visitorsToBeApplied, fileWithAsts.inputFileContext, fileWithAsts.tree);
      progressReport.nextFile();
    }
    progressReport.stop();
    return true;
  }

  private static String progressReportThreadName(String language, String phase) {
    return "Progress of the " + language + " " + phase;
  }

  private record FileWithAst(InputFileContext inputFileContext, Tree tree) {
  }
}
