/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;

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

  protected void highlight(HasTextRange range, TypeOfText typeOfText) {
    if (range.textRange().start().equals(range.textRange().end())) {
      return;
    }

    newHighlighting.highlight(inputFile.newRange(range.textRange().start().line(), range.textRange().start().lineOffset(), range.textRange().end().line(),
        range.textRange().end().lineOffset()), typeOfText);
  }

  private void highlightComments(Tree tree) {
    if (tree instanceof HasComments) {
      ((HasComments) tree).comments().forEach(comment -> highlight(comment, COMMENT));
    }
  }
}
