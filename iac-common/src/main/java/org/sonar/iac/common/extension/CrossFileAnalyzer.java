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

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.ProgressReport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrossFileAnalyzer extends AbstractAnalyzer {

  public CrossFileAnalyzer(String repositoryKey, TreeParser<? extends Tree> parser, List<TreeVisitor<InputFileContext>> visitors, DurationStatistics statistics) {
    super(repositoryKey, parser, visitors, statistics);
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
        logParsingError(e);
        inputFileContext.reportParseError(repositoryKey, e.getPosition());
      }

      progressReport.nextFile();
    }

    // Visit files
    for (TreeVisitor<InputFileContext> visitor : visitors) {
      for (FileWithAst fileWithAst : filesWithAst) {
        if (sensorContext.isCancelled()) {
          return false;
        }

        visit(visitor, fileWithAst.inputFileContext, fileWithAst.tree);
      }
    }

    return true;
  }

  private record FileWithAst(InputFileContext inputFileContext, Tree tree) {
  }
}
