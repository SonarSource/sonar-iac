/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.common.yaml.visitors;

import java.io.IOException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class YamlMetricsVisitor extends MetricsVisitor {

  public YamlMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter) {
    super(fileLinesContextFactory, noSonarFilter);
  }

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    super.before(ctx, root);
    try {
      analyzeFileContentForLoc(ctx.inputFile);
    } catch (IOException e) {
      throw new ParseException("Can not read file for metric calculation", null, e.getMessage());
    }
  }

  /**
   * Analyze line by line and do not use nodes from YAML parser since their text range contains trailing lines with whitespaces which should
   * not be counted as LOC
   */
  private void analyzeFileContentForLoc(InputFile inputFile) throws IOException {
    int lineNumber = 1;
    String[] lines = inputFile.contents().split("(\\r\\n|\\r|\\n)");
    for (String line : lines) {
      line = line.trim();
      if (!(line.isBlank() || line.startsWith("#"))) {
        linesOfCode().add(lineNumber);
      }
      lineNumber++;
    }
  }

  @Override
  protected void languageSpecificMetrics() {
    register(YamlTree.class, (ctx, tree) -> addCommentLines(tree.metadata().comments()));
  }
}
