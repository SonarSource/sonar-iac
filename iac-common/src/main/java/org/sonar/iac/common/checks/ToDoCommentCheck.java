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
package org.sonar.iac.common.checks;

import java.util.List;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;

public class ToDoCommentCheck implements IacCheck {

  private static final String MESSAGE = "Complete the task associated to this \"TODO\" comment.";

  @Override
  public void initialize(InitContext context) {
    context.register(Tree.class, (ctx, tree) -> {
      List<Comment> comments = tree.comments();
      if (!comments.isEmpty()) {
        for (Comment comment : comments) {
          if(comment.value().contains("TODO")) {
            ctx.reportIssue(comment.textRange(), MESSAGE);
          }
        }
      }
    });
  }
}
