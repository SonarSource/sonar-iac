/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class YamlMetricsVisitor extends MetricsVisitor {

  public YamlMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter) {
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
    register(YamlTree.class, (ctx, tree) -> addCommentLines(tree.metadata().comments()));
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
