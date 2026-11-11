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
package org.sonar.iac.terraform.visitors;

import java.util.Set;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.NoSonarNextLineResolver;
import org.sonar.iac.common.extension.visitors.NoSonarVisitor;
import org.sonar.iac.terraform.api.tree.HeredocLiteralTree;

public class TerraformNoSonarVisitor extends NoSonarVisitor {

  private NoSonarNextLineResolver noSonarNextLineResolver;

  public TerraformNoSonarVisitor(NoSonarFilter noSonarFilter) {
    super(noSonarFilter);
    this.noSonarNextLineResolver = new NoSonarNextLineResolver();
  }

  @Override
  protected void registerCommentConsumers() {
    register(IacToken.class, (ctx, token) -> {
      addNoSonarLines(token.comments());
      if (!token.value().isBlank()) {
        noSonarNextLineResolver.addCodeLines(token.textRange());
      }
    });
    register(HeredocLiteralTree.class, (ctx, heredoc) -> noSonarNextLineResolver.addElement(heredoc.textRange()));
  }

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    super.before(ctx, root);
    noSonarNextLineResolver = new NoSonarNextLineResolver();
  }

  /**
   * The content of a heredoc cannot hold a Terraform comment. Hence, a comment suppressing issues on the line where a heredoc
   * starts, or written alone on its line right above it, covers the whole heredoc.
   */
  @Override
  protected void after(InputFileContext ctx, Tree root) {
    noSonarLines().addAll(noSonarNextLineResolver.coveredElementLines(Set.copyOf(noSonarLines())));
    super.after(ctx, root);
  }
}
