/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.checks;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.Tree;

@Rule(key = "S1135")
public class ToDoCommentCheck implements IacCheck {

  private static final String MESSAGE = "Complete the task associated to this \"TODO\" comment.";

  @Override
  public void initialize(InitContext context) {
    context.register(Tree.class, (ctx, tree) -> {
      if (tree instanceof HasComments treeWithComments) {
        List<Comment> comments = treeWithComments.comments();
        for (Comment comment : comments) {
          if (comment.value().contains("TODO")) {
            ctx.reportIssue(comment.textRange(), MESSAGE);
          }
        }
      }
    });
  }
}
