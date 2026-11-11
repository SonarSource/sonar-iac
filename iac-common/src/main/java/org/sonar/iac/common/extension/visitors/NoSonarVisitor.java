/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.Tree;

/**
 * Registers with {@link NoSonarFilter} the lines holding a comment which contains {@value #NOSONAR_PREFIX}, so that no issue is
 * reported on them.
 * <p>
 * Issues are filtered when they are saved, hence this visitor must run before the checks visitor. Contrary to {@link MetricsVisitor},
 * it also runs in SonarQube for IDE.
 */
public class NoSonarVisitor extends TreeVisitor<InputFileContext> {

  public static final String NOSONAR_PREFIX = "NOSONAR";

  private final NoSonarFilter noSonarFilter;
  private Set<Integer> noSonarLines = new HashSet<>();

  public NoSonarVisitor(NoSonarFilter noSonarFilter) {
    this.noSonarFilter = noSonarFilter;
    registerCommentConsumers();
  }

  /**
   * Registers the consumers reading the comments of the tree. By default, the comments attached to the tokens are read.
   */
  protected void registerCommentConsumers() {
    register(IacToken.class, (ctx, token) -> addNoSonarLines(token.comments()));
  }

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    noSonarLines = new HashSet<>();
  }

  @Override
  protected void after(InputFileContext ctx, Tree root) {
    if (!noSonarLines.isEmpty()) {
      noSonarFilter.noSonarInFile(ctx.inputFile, noSonarLines);
    }
  }

  protected void addNoSonarLines(List<Comment> comments) {
    for (Comment comment : comments) {
      List<String> lines = comment.contentText().lines().toList();
      int startLine = comment.textRange().start().line();
      for (int i = 0; i < lines.size(); i++) {
        if (lines.get(i).contains(NOSONAR_PREFIX)) {
          noSonarLines.add(startLine + i);
        }
      }
    }
  }

  public Set<Integer> noSonarLines() {
    return noSonarLines;
  }
}
