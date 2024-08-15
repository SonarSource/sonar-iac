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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.VariableNode;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S1874")
public class DeprecatedCodeCheck implements IacCheck {
  private static final String MESSAGE = "Remove this deprecated use of \"Capabilities.KubeVersion.GitVersion\", use \"Capabilities" +
    ".KubeVersion.Version\" instead.";

  @Override
  public void initialize(InitContext init) {
    init.register(CommandNode.class, DeprecatedCodeCheck::checkTree);
  }

  private static void checkTree(CheckContext ctx, CommandNode commandNode) {
    var kubernetesContext = (KubernetesCheckContext) ctx;
    for (Node node : commandNode.arguments()) {
      if (shouldReportNode(node)) {
        kubernetesContext.reportIssueNoLineShift(node.textRange(), MESSAGE);
      }
    }
  }

  private static boolean shouldReportNode(Node node) {
    if (node instanceof FieldNode fieldNode) {
      return isDeprecatedGitVersionFromDotLevel(fieldNode.identifiers());
    } else if (node instanceof VariableNode variableNode) {
      return isDeprecatedGitVersionFromTopLevel(variableNode.idents());
    }
    return false;
  }

  private static boolean isDeprecatedGitVersionFromDotLevel(List<String> identifiers) {
    return identifiers.size() == 3
      && "Capabilities".equals(identifiers.get(0))
      && "KubeVersion".equals(identifiers.get(1))
      && "GitVersion".equals(identifiers.get(2));
  }

  private static boolean isDeprecatedGitVersionFromTopLevel(List<String> identifiers) {
    return identifiers.size() == 4
      && "$".equals(identifiers.get(0))
      && "Capabilities".equals(identifiers.get(1))
      && "KubeVersion".equals(identifiers.get(2))
      && "GitVersion".equals(identifiers.get(3));
  }
}
