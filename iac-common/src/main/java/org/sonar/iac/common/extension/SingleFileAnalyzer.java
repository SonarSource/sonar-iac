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

import java.util.Collection;
import java.util.List;

public class SingleFileAnalyzer extends AbstractAnalyzer {

  public SingleFileAnalyzer(String repositoryKey, TreeParser<? extends Tree> parser, List<TreeVisitor<InputFileContext>> visitors, DurationStatistics statistics) {
    super(repositoryKey, parser, visitors, statistics);
  }

  public boolean analyseFiles(SensorContext sensorContext, Collection<InputFile> inputFiles, ProgressReport progressReport) {
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

  private void analyseFile(InputFileContext inputFileContext) {
    var content = readContent(inputFileContext);
    if (content == null) {
      return;
    }

    Tree tree = statistics.time("Parse", () -> parse(content, inputFileContext));

    visit(inputFileContext, tree);
  }

  public void visit(InputFileContext inputFileContext, Tree tree) {
    for (TreeVisitor<InputFileContext> visitor : visitors) {
      visit(visitor, inputFileContext, tree);
    }
  }
}
