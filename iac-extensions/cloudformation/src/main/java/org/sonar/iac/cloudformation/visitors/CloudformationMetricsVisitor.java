/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.visitors;

import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;

public class CloudformationMetricsVisitor extends MetricsVisitor {

  public CloudformationMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter) {
    super(fileLinesContextFactory, noSonarFilter);
  }

  @Override
  protected void languageSpecificMetrics() {
    register(ScalarTree.class, (ctx, tree) -> {
      for (int i = tree.textRange().start().line(); i <= endLine(tree); i++) {
        linesOfCode().add(i);
      }
    });
    register(MappingTree.class, (ctx, tree) -> addBraces(tree));
    register(SequenceTree.class, (ctx, tree) -> addBraces(tree));
    register(CloudformationTree.class, (ctx, tree) -> addCommentLines(tree.comments()));
  }

  // SONARIAC-82 Lines which contain only brackets should also be counted for metrics
  private void addBraces(HasTextRange tree) {
    linesOfCode().add(tree.textRange().start().line());
    linesOfCode().add(endLine(tree));
  }

  // SONARIAC-80 Do not add line to range if tree ends with new line
  private static int endLine(HasTextRange tree) {
    int endLine = tree.textRange().end().line();
    return tree.textRange().end().lineOffset() == 0 ? (endLine - 1) : endLine;
  }
}
