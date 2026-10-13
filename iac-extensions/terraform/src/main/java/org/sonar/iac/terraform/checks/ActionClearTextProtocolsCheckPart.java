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
package org.sonar.iac.terraform.checks;

import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

import static org.sonar.iac.common.checks.network.UrlUtils.findUnencryptedUrlsOffsets;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isAction;
import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_CLEAR_TEXT_PROTOCOL;

/** Runs the common unencrypted URL detector on every string literal in the config block of an action. */
public class ActionClearTextProtocolsCheckPart implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, (ctx, tree) -> {
      if (isAction(tree)) {
        var urlVisitor = new UnencryptedUrlVisitor(ctx);
        PropertyUtils.getAll(tree, "config", BlockTree.class).forEach(config -> urlVisitor.scan(new TreeContext(), config));
      }
    });
  }

  private static class UnencryptedUrlVisitor extends TreeVisitor<TreeContext> {

    private UnencryptedUrlVisitor(CheckContext checkContext) {
      register(LiteralExprTree.class, (ctx, literal) -> {
        SyntaxToken token = literal.token();
        findUnencryptedUrlsOffsets(token.textRange().start(), token.value())
          .forEach(url -> checkContext.reportIssue(url, MESSAGE_CLEAR_TEXT_PROTOCOL));
      });
    }
  }
}
