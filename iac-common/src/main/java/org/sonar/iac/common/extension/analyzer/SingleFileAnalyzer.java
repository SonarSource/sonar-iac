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

public class SingleFileAnalyzer extends AbstractAnalyzer {

  private final List<TreeVisitor<InputFileContext>> visitors;

  public SingleFileAnalyzer(String repositoryKey, TreeParser<? extends Tree> parser, List<TreeVisitor<InputFileContext>> visitors, DurationStatistics statistics) {
    super(repositoryKey, parser, statistics);
    this.visitors = visitors;
  }

  public boolean analyseFiles(SensorContext sensorContext, Collection<InputFile> inputFiles, String languageName) {
    List<String> filenames = inputFiles.stream().map(InputFile::toString).toList();
    var progressReport = new ProgressReport("Progress of the " + languageName + " analysis", PROGRESS_REPORT_PERIOD_MILLIS);
    progressReport.start(filenames);

    for (InputFile inputFile : inputFiles) {
      if (sensorContext.isCancelled()) {
        progressReport.cancel();
        return false;
      }
      var inputFileContext = createInputFileContext(sensorContext, inputFile);
      try {
        analyseFile(inputFileContext);
      } catch (ParseException e) {
        reportParseError(e, inputFileContext);
      }
      progressReport.nextFile();
    }
    progressReport.stop();
    return true;
  }

  private void analyseFile(InputFileContext inputFileContext) {
    var content = readContent(inputFileContext);
    if (content == null) {
      return;
    }

    Tree tree = statistics.time("Parse", () -> parse(content, inputFileContext));

    visit(visitors, inputFileContext, tree);
  }
}
