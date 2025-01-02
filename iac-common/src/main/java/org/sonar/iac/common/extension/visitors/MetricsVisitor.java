/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.common.extension.visitors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;

public abstract class MetricsVisitor extends TreeVisitor<InputFileContext> {

  public static final String NOSONAR_PREFIX = "NOSONAR";
  private final FileLinesContextFactory fileLinesContextFactory;
  private final NoSonarFilter noSonarFilter;
  private final SensorTelemetryMetrics sensorTelemetryMetrics;

  private Set<Integer> linesOfCode;
  private Set<Integer> commentLines;
  private Set<Integer> noSonarLines;

  protected MetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, SensorTelemetryMetrics sensorTelemetryMetrics) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    this.sensorTelemetryMetrics = sensorTelemetryMetrics;
    languageSpecificMetrics();
  }

  protected void languageSpecificMetrics() {
    register(IacToken.class, defaultMetricsVisitor());
  }

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    linesOfCode = new HashSet<>();
    commentLines = new HashSet<>();
    noSonarLines = new HashSet<>();
  }

  @Override
  protected void after(InputFileContext ctx, Tree root) {
    var linesOfCodeSize = linesOfCode.size();
    saveMetric(ctx, CoreMetrics.NCLOC, linesOfCodeSize);
    saveMetric(ctx, CoreMetrics.COMMENT_LINES, commentLines.size());

    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(ctx.inputFile);
    linesOfCode.forEach(line -> fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, 1));
    fileLinesContext.save();
    noSonarFilter.noSonarInFile(ctx.inputFile, noSonarLines);
    sensorTelemetryMetrics.addLinesOfCode(linesOfCodeSize);
  }

  protected void addCommentLines(List<Comment> comments) {
    for (Comment comment : comments) {
      String[] lines = comment.contentText().split("(\r)?\n|\r", -1);
      int currentLine = comment.textRange().start().line();
      for (String line : lines) {
        if (line.contains(NOSONAR_PREFIX)) {
          noSonarLines.add(currentLine);
        } else if (hasContent(line)) {
          commentLines.add(currentLine);
        }
        currentLine++;
      }
    }
  }

  protected boolean hasContent(String commentLine) {
    return commentLine.chars().anyMatch(Character::isLetterOrDigit);
  }

  private static void saveMetric(InputFileContext ctx, Metric<Integer> metric, Integer value) {
    ctx.sensorContext.<Integer>newMeasure()
      .on(ctx.inputFile)
      .forMetric(metric)
      .withValue(value)
      .save();
  }

  public Set<Integer> linesOfCode() {
    return linesOfCode;
  }

  public Set<Integer> commentLines() {
    return commentLines;
  }

  public Set<Integer> noSonarLines() {
    return noSonarLines;
  }

  public <T extends IacToken> BiConsumer<InputFileContext, T> defaultMetricsVisitor() {
    return (ctx, token) -> {
      if (!(token.value().trim().isEmpty())) {
        TextRange range = token.textRange();
        for (int i = range.start().line(); i <= range.end().line(); i++) {
          linesOfCode().add(i);
        }
      }
      addCommentLines(token.comments());
    };
  }
}
