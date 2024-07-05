/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.kubernetes.checks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.helm.tree.api.ActionNode;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.CommentNode;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;
import org.sonar.iac.helm.tree.api.TextNode;
import org.sonar.iac.helm.tree.api.VariableNode;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S6893")
public class WhitespaceBracesCheck implements ChecksGoTemplate, IacCheck {
  private static final String MESSAGE = "Add a whitespace after {{ or before }} in the template directive.";
  private static final int INVALID_DISTANCE = "{{".length();

  private TextRange lastNodeTextRange = null;

  @Override
  public void initialize(InitContext init) {
    init.register(GoTemplateTree.class, (ctx, tree) -> {
      lastNodeTextRange = null;
    });
    init.register(TextNode.class, (ctx, node) -> handle((KubernetesCheckContext) ctx, node));
    init.register(ActionNode.class, (ctx, node) -> handle((KubernetesCheckContext) ctx, node));
    init.register(CommentNode.class, (ctx, node) -> handle((KubernetesCheckContext) ctx, node));
  }

  private void handle(KubernetesCheckContext ctx, Node node) {
    var currentTextRange = textRange(node);
    if (lastNodeTextRange != null) {
      var dist = distance(lastNodeTextRange.end(), currentTextRange.start());
      System.out.println("AAA dist " + dist);
      if (dist == INVALID_DISTANCE) {
        var textRange = TextRanges.range(
          lastNodeTextRange.end().line(),
          lastNodeTextRange.end().lineOffset(),
          currentTextRange.start().line(),
          currentTextRange.start().lineOffset());
        System.out.println("AAA report on " + textRange);
        ctx.reportIssueNoLineShift(textRange, MESSAGE);
      }
    }

    lastNodeTextRange = currentTextRange;
    System.out.println("AAA lastNodeTextRange " + lastNodeTextRange + " node " + node);
  }

  private int distance(TextPointer end, TextPointer start) {
    if (end.line() == start.line()) {
      return start.lineOffset() - end.lineOffset();
    } else {
      return start.lineOffset();
    }
  }

  private TextRange textRange(Node node) {
    if (node instanceof ActionNode) {
      // It is temporary workaround for SONARIAC-1530 Wrong text ranges in "ActionNode" (Go AST)
      List<TextRange> textRanges = new ArrayList<>(node.children().stream().map(Tree::textRange).toList());
      textRanges.add(node.textRange());
      return TextRanges.merge(textRanges);
    }
    return node.textRange();
  }
}
