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
package org.sonar.iac.common.extension.visitors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOG = LoggerFactory.getLogger(SyntaxHighlightingVisitor.class);

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
      LOG.debug("Tried to highlight a tree with an empty or invalid range. Skipping it.");
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
    if (tree instanceof HasComments) {
      ((HasComments) tree).comments().forEach(comment -> highlight(comment, COMMENT));
    }
  }
}
