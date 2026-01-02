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
package org.sonar.iac.terraform.visitors;

import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.ANNOTATION;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.CONSTANT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD_LIGHT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

public class TerraformHighlightingVisitor extends SyntaxHighlightingVisitor {

  @Override
  protected void languageSpecificHighlighting() {
    register(BlockTree.class, (ctx, tree) -> highlight(tree.key(), CONSTANT));
    register(LabelTree.class, (ctx, tree) -> highlight(tree, KEYWORD));
    register(AttributeAccessTree.class, (ctx, tree) -> highlight(tree, KEYWORD));
    register(LiteralExprTree.class, (ctx, tree) -> {
      if (tree.is(TerraformTree.Kind.STRING_LITERAL, TerraformTree.Kind.TEMPLATE_STRING_PART_LITERAL)) {
        highlight(tree, STRING);
      } else if (tree.is(TerraformTree.Kind.NUMERIC_LITERAL)) {
        highlight(tree, KEYWORD_LIGHT);
      } else {
        highlight(tree, CONSTANT);
      }
    });
    register(AttributeTree.class, (ctx, tree) -> highlight(tree.key(), ANNOTATION));
    register(ObjectElementTree.class, (ctx, tree) -> highlight(tree.key(), ANNOTATION));
  }

}
