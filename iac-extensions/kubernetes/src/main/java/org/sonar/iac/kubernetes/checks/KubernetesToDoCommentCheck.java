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
package org.sonar.iac.kubernetes.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S1135")
public class KubernetesToDoCommentCheck implements IacCheck {

  private static final String MESSAGE = "Complete the task associated to this \"TODO\" comment.";

  @Override
  public void initialize(InitContext context) {
    context.register(Tree.class, (CheckContext ctx, Tree tree) -> {
      if (tree instanceof HasComments treeWithComments) {
        checkTodoComments(treeWithComments, (KubernetesCheckContext) ctx);
      }
    });
  }

  private static void checkTodoComments(HasComments treeWithComments, KubernetesCheckContext ctx) {
    var comments = treeWithComments.comments();
    for (var comment : comments) {
      if (comment.value().contains("TODO")) {
        if (treeWithComments instanceof Node) {
          ctx.reportIssueNoLineShift(comment.textRange(), MESSAGE);
        } else {
          ctx.reportIssue(comment.textRange(), MESSAGE);
        }
      }
    }
  }
}
