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
package org.sonar.iac.kubernetes.visitors;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.sonar.iac.common.yaml.YamlFileUtils.splitLines;

/**
 * This visitor has two goals:
 * - retrieve the comments which mention line numbers and store the information
 * - store the size of each line from the original source file (InputFileContext)
 * From those information, the {@link AdjustableChecksVisitor} will be able to raise issue from the transformed code tree nodes into
 * the original source code.
 * The information are transmitted between the two visitors using a dedicated {@link LocationShifter} object.
 */
public class CommentLocationVisitor extends TreeVisitor<InputFileContext> {
  private static final Logger LOG = LoggerFactory.getLogger(CommentLocationVisitor.class);
  private static final Pattern CONTAINS_LINE_NUMBER_OR_RANGE = Pattern.compile("#(?<rangeStart>\\d++)(:(?<rangeEnd>\\d++))?(\\s#\\d++:?\\d*+)*+$");
  private final LocationShifter shifter;

  public CommentLocationVisitor(LocationShifter shifter) {
    this.shifter = shifter;
    initialize();
  }

  private void initialize() {
     register(YamlTree.class, this::visitComment);
  }

  @Override
  protected void visit(InputFileContext ctx, @Nullable Tree node) {
    if (isFileWithHelmTemplate(node)) {
      readLinesSizes(ctx);
      super.visit(ctx, node);
    } else if (isNotFileTree(node)) {
      super.visit(ctx, node);
    }
  }

  private static boolean isFileWithHelmTemplate(@Nullable Tree node) {
    return node instanceof FileTree && ((FileTree) node).template() == FileTree.Template.HELM;
  }

  private static boolean isNotFileTree(@Nullable Tree node) {
    return node != null && !(node instanceof FileTree);
  }

  public void readLinesSizes(InputFileContext ctx) {
    try {
      var lines = splitLines(ctx.inputFile.contents());
      for (var lineNumber = 1; lineNumber <= lines.length; lineNumber++) {
        shifter.addLineSize(ctx, lineNumber, lines[lineNumber - 1].length());
      }
    } catch (IOException e) {
      LOG.error("Unable to read file: {}.", ctx.inputFile.uri(), e);
    }
  }

  public void visitComment(InputFileContext ctx, YamlTree tree) {
    tree.metadata().comments().forEach(comment -> processComment(ctx, comment));
  }

  private void processComment(InputFileContext ctx, Comment comment) {
    var matcher = CONTAINS_LINE_NUMBER_OR_RANGE.matcher(comment.value());
    if (matcher.find()) {
      int lineCommentLocation = comment.textRange().start().line();
      var lineCommentRangeStart = Integer.parseInt(matcher.group("rangeStart"));
      var lineCommentRangeEnd = Optional.ofNullable(matcher.group("rangeEnd")).map(Integer::parseInt).orElse(lineCommentRangeStart);
      shifter.addShiftedLine(ctx, lineCommentLocation, lineCommentRangeStart, lineCommentRangeEnd);
    } else {
      LOG.debug("Line number comment not detected, comment: {}", comment.value());
    }
  }
}
