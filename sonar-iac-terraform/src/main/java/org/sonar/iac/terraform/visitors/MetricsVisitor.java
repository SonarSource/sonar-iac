/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.visitors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.Comment;
import org.sonar.iac.common.TextRange;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.plugin.InputFileContext;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class MetricsVisitor extends TreeVisitor<InputFileContext> {

  public static final String NOSONAR_PREFIX = "NOSONAR";
  private final FileLinesContextFactory fileLinesContextFactory;
  private final NoSonarFilter noSonarFilter;

  Set<Integer> linesOfCode;
  Set<Integer> commentLines;
  Set<Integer> noSonarLines;

  public MetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;

    register(SyntaxToken.class, (ctx, token) -> {
      if (!token.value().isEmpty()) {
        TextRange range = token.textRange();
        for (int i = range.start().line(); i <= range.end().line(); i++) {
          linesOfCode.add(i);
        }
      }
      addCommentLines(token.comments());
    });
  }

  @Override
  protected void before(InputFileContext ctx, TerraformTree root) {
    linesOfCode = new HashSet<>();
    commentLines = new HashSet<>();
    noSonarLines = new HashSet<>();
  }

  @Override
  protected void after(InputFileContext ctx, TerraformTree root) {
    saveMetric(ctx, CoreMetrics.NCLOC, linesOfCode.size());
    saveMetric(ctx, CoreMetrics.COMMENT_LINES, commentLines.size());

    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(ctx.inputFile);
    linesOfCode.forEach(line -> fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, 1));
    fileLinesContext.save();
    noSonarFilter.noSonarInFile(ctx.inputFile, noSonarLines);
  }

  private void addCommentLines(List<Comment> comments) {
    for (Comment comment : comments) {
      String[] lines = comment.contentText().split("(\r)?\n|\r", -1);
      int currentLine = comment.textRange().start().line();
      for (String line : lines) {
        if (line.contains(NOSONAR_PREFIX)) {
          noSonarLines.add(currentLine);
        } else if (!isBlank(line)) {
          commentLines.add(currentLine);
        }
        currentLine++;
      }
    }
  }

  private static boolean isBlank(String line) {
    return line.chars().noneMatch(Character::isLetterOrDigit);
  }

  private static void saveMetric(InputFileContext ctx, Metric<Integer> metric, Integer value) {
    ctx.sensorContext.<Integer>newMeasure()
      .on(ctx.inputFile)
      .forMetric(metric)
      .withValue(value)
      .save();
  }
}
