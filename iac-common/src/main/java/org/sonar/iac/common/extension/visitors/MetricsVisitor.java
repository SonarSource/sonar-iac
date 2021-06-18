/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.extension.visitors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;

public abstract class MetricsVisitor extends TreeVisitor<InputFileContext> {

  public static final String NOSONAR_PREFIX = "NOSONAR";
  private final FileLinesContextFactory fileLinesContextFactory;
  private final NoSonarFilter noSonarFilter;

  private Set<Integer> linesOfCode;
  private Set<Integer> commentLines;
  private Set<Integer> noSonarLines;

  protected MetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    languageSpecificMetrics();
  }

  protected abstract void languageSpecificMetrics();

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    linesOfCode = new HashSet<>();
    commentLines = new HashSet<>();
    noSonarLines = new HashSet<>();
  }

  @Override
  protected void after(InputFileContext ctx, Tree root) {
    saveMetric(ctx, CoreMetrics.NCLOC, linesOfCode.size());
    saveMetric(ctx, CoreMetrics.COMMENT_LINES, commentLines.size());

    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(ctx.inputFile);
    linesOfCode.forEach(line -> fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, 1));
    fileLinesContext.save();
    noSonarFilter.noSonarInFile(ctx.inputFile, noSonarLines);
  }

  protected void addCommentLines(List<Comment> comments) {
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

  public Set<Integer> linesOfCode() {
    return linesOfCode;
  }

  public Set<Integer> commentLines() {
    return commentLines;
  }

  public Set<Integer> noSonarLines() {
    return noSonarLines;
  }
}
