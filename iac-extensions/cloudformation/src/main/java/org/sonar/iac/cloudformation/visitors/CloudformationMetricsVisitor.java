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
package org.sonar.iac.cloudformation.visitors;

import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
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
    register(CloudformationTree.class, (ctx, tree) -> addCommentLines(tree.comments()));
  }

  private static int endLine(ScalarTree tree) {
    return tree.textRange().end().line() - (tree.value().endsWith("\n") ? 1 : 0);
  }
}
