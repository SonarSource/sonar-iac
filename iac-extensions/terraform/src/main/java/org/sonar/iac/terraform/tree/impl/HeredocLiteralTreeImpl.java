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
package org.sonar.iac.terraform.tree.impl;

import java.util.function.Supplier;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.HeredocLiteralTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

/**
 * Heredoc literal node produced by the Terraform parser.
 *
 * <p>The full heredoc text (including the surrounding {@code <<TAG ... TAG} markers) remains accessible
 * through {@link #value()} so existing {@code LiteralExprTree} consumers behave as before. In addition,
 * the body is exposed via {@link #content()}, a structured view built on demand by the supplier passed
 * from the parser (typically wrapping {@code HeredocContentParser}).
 *
 * <p>The structured content is built lazily on the first {@link #content()} call and then cached, so a
 * heredoc whose body is never inspected (e.g. a {@code user_data} blob, or any heredoc when the policy
 * checks are disabled) pays no parsing cost. The {@code content()} subtree is NOT included in
 * {@link #children()} because its synthetic nodes share the heredoc's text range and would confuse
 * source-fidelity rebuilders and position-based visitors. Consumers that need the structured content
 * call {@link #content()} directly.
 */
public class HeredocLiteralTreeImpl extends LiteralExprTreeImpl implements HeredocLiteralTree {

  private final Supplier<ExpressionTree> contentSupplier;
  private ExpressionTree content;
  private boolean contentBuilt;

  public HeredocLiteralTreeImpl(SyntaxToken token, Supplier<ExpressionTree> contentSupplier) {
    super(Kind.HEREDOC_LITERAL, token);
    this.contentSupplier = contentSupplier;
  }

  @Override
  public ExpressionTree content() {
    if (!contentBuilt) {
      content = contentSupplier.get();
      contentBuilt = true;
    }
    return content;
  }
}
