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
package org.sonar.iac.common.extension.analyzer;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.ProgressReport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

  public boolean analyseFiles(SensorContext sensorContext, Collection<InputFile> inputFiles, ProgressReport progressReport) {
    List<InputFileContext> inputFileContextList = inputFiles.stream()
      .map(inputFile -> createInputFileContext(sensorContext, inputFile))
      .toList();

    // Parse files
    List<FileWithAst> filesWithAst = new ArrayList<>();
    for (InputFileContext inputFileContext : inputFileContextList) {
      if (sensorContext.isCancelled()) {
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

      // TODO SONARIAC-1511 Change the usage of ProgressReport do to proper reporting in cross file analysis
      progressReport.nextFile();
    }

    // Visit files
    for (FileWithAst fileWithAst : filesWithAst) {
      if (sensorContext.isCancelled()) {
        return false;
      }

      visit(visitors, fileWithAst.inputFileContext, fileWithAst.tree);
    }

    // Apply check visitor
    for (FileWithAst fileWithAst : filesWithAst) {
      if (sensorContext.isCancelled()) {
        return false;
      }

      visit(List.of(checksVisitor), fileWithAst.inputFileContext, fileWithAst.tree);
    }

    return true;
  }

  private record FileWithAst(InputFileContext inputFileContext, Tree tree) {
  }
}
