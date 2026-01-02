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
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

class GoTemplateAstVerifierTest {
  @Test
  void testVisitor() {
    KubernetesVerifier.verify("ChecksGoTemplate/helm/templates/capabilities-pod.yaml", new DummyChecksGoAst(), List.of(
      Verifier.issue(12, 18, 12, 38, "Check hardcoded issue from Go AST"),
      Verifier.issue(25, 18, 25, 38, "Check hardcoded issue from Go AST")));
  }

  @Test
  void testVisitorWithGoComments() {
    KubernetesVerifier.verify("ChecksGoTemplate/helm/templates/capabilities-pod.yaml", new DummyChecksGoAst());
  }

  private static class DummyChecksGoAst implements IacCheck {
    @Override
    public void initialize(InitContext init) {
      init.register(FieldNode.class, (ctx, node) -> {
        if (node.identifiers().size() > 1 && node.identifiers().get(1).equals("capabilities")) {
          ((KubernetesCheckContext) ctx).reportIssueNoLineShift(node.textRange(), "Check hardcoded issue from Go AST");
        }
      });
    }
  }
}
