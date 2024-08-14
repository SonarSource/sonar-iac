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
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

class ChecksGoTemplateTest {
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

  private static class DummyChecksGoAst implements ChecksGoTemplate, IacCheck {
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
