/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.visitors;

import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.CONSTANT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

public class TerraformHighlightingVisitor extends SyntaxHighlightingVisitor {

  @Override
  protected void languageSpecificHighlighting() {
    register(BlockTree.class,  (ctx, tree) -> highlight(tree.identifier(), KEYWORD));
    register(LabelTree.class, (ctx, tree) -> highlight(tree, STRING));
    register(LiteralExprTree.class, (ctx, tree) -> highlight(tree,  tree.is(TerraformTree.Kind.STRING_LITERAL) ? STRING : CONSTANT));
  }

}
