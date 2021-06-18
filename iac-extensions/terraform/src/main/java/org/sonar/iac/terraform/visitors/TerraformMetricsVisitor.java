/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.visitors;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class TerraformMetricsVisitor extends MetricsVisitor {

  public TerraformMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter) {
    super(fileLinesContextFactory, noSonarFilter);
  }

  @Override
  protected void languageSpecificMetrics() {
    register(SyntaxToken.class, (ctx, token) -> {
      if (!token.value().isEmpty()) {
        TextRange range = token.textRange();
        for (int i = range.start().line(); i <= range.end().line(); i++) {
          linesOfCode().add(i);
        }
      }
      addCommentLines(token.comments());
    });
  }
}
