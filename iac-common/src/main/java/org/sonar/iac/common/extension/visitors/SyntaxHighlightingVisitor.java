/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.extension.visitors;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;

public abstract class SyntaxHighlightingVisitor extends TreeVisitor<InputFileContext> {

  private NewHighlighting newHighlighting;
  private InputFile inputFile;

  protected SyntaxHighlightingVisitor() {
    register(Tree.class, (ctx, tree) -> highlightComments(tree));
    languageSpecificHighlighting();
  }

  protected abstract void languageSpecificHighlighting();

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    inputFile = ctx.inputFile;
    newHighlighting = ctx.sensorContext.newHighlighting()
      .onFile(inputFile);
  }

  @Override
  protected void after(InputFileContext ctx, Tree root) {
    newHighlighting.save();
  }

  protected void highlight(HasTextRange tree, TypeOfText typeOfText) {
    highlight(tree.textRange(), typeOfText);
  }

  protected void highlight(TextRange textRange, TypeOfText typeOfText) {
    if (!TextRanges.isValidAndNotEmpty(textRange)) {
      // Nothing to do here, these are often valid cases like an empty value in yaml
      return;
    }

    newHighlighting.highlight(
      inputFile.newRange(
        textRange.start().line(),
        textRange.start().lineOffset(),
        textRange.end().line(),
        textRange.end().lineOffset()),
      typeOfText);
  }

  public void highlightComments(Tree tree) {
    if (tree instanceof HasComments treeWithComments) {
      treeWithComments.comments().forEach(comment -> highlight(comment, COMMENT));
    }
  }
}
