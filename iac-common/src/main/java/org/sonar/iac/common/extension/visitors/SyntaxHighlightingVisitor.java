/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.extension.visitors;

import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;

public abstract class SyntaxHighlightingVisitor extends TreeVisitor<InputFileContext> {

  private NewHighlighting newHighlighting;

  protected SyntaxHighlightingVisitor() {
    register(Tree.class, (ctx, tree) -> {
      if (tree instanceof HasComments) {
        ((HasComments) tree).comments().forEach(comment -> highlight(comment, COMMENT));
      }
    });
    languageSpecificHighlighting();
  }

  protected abstract void languageSpecificHighlighting();

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    newHighlighting = ctx.sensorContext.newHighlighting()
      .onFile(ctx.inputFile);
  }

  @Override
  protected void after(InputFileContext ctx, Tree root) {
    newHighlighting.save();
  }

  protected void highlight(HasTextRange range, TypeOfText typeOfText) {
    newHighlighting.highlight(range.textRange(), typeOfText);
  }
}
