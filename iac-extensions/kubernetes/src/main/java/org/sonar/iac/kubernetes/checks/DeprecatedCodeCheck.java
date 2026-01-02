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

import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.VariableNode;
import org.sonar.iac.kubernetes.model.Chart;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S1874")
public class DeprecatedCodeCheck implements IacCheck {
  private static final String MESSAGE = "\"Capabilities.KubeVersion.GitVersion\" is deprecated since Helm 3, " +
    "use \"Capabilities.KubeVersion.Version\" instead.";

  @Override
  public void initialize(InitContext init) {
    init.register(CommandNode.class, DeprecatedCodeCheck::checkTree);
  }

  private static void checkTree(CheckContext ctx, CommandNode commandNode) {
    var kubernetesContext = (KubernetesCheckContext) ctx;

    boolean isNotDeprecated = Optional.ofNullable(kubernetesContext.projectContext().getChart())
      .map(Chart::apiVersion)
      .map("v1"::equals)
      .orElse(true);
    if (isNotDeprecated) {
      // GitVersion is deprecated since Helm 3; if apiVersion is not v2, or absent, we assume it's not Helm >=3
      return;
    }

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
