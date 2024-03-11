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
package org.sonar.iac.common.yaml.tree;

import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.Node;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.ParseException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record YamlTreeMetadata(String tag, TextRange textRange, List<Comment> comments) implements HasTextRange {

  public static YamlTreeMetadata fromNode(String tag, Node node) {
    return new YamlTreeMetadata(tag, range(node), comments(node));
  }

  public static YamlTreeMetadata fromNode(Node node) {
    return fromNode(tag(node), node);
  }

  public static YamlTreeMetadata fromNodes(String tag, Node firstNode, Node secondNode) {
    return new YamlTreeMetadata(tag, TextRanges.merge(List.of(range(firstNode), range(secondNode))), Collections.emptyList());
  }

  public static List<Comment> comments(Node node) {
    // For now we group all comments together. This might change, once we have a reason to separate them.
    List<Comment> comments = new ArrayList<>(comments(node.getBlockComments()));
    comments.addAll(comments(node.getInLineComments()));
    comments.addAll(comments(node.getEndComments()));
    return comments;
  }

  public static List<Comment> comments(@Nullable List<CommentLine> commentLines) {
    if (commentLines == null) {
      return Collections.emptyList();
    }
    List<Comment> comments = new ArrayList<>();
    for (CommentLine comment : commentLines) {
      comments.add(comment(comment));
    }
    return comments;
  }

  private static Comment comment(CommentLine comment) {
    // We prefix the comment value with # as it is already stripped away when arrive at this point.
    return new CommentImpl('#' + comment.getValue(), comment.getValue(), range(comment.getStartMark(), comment.getEndMark()));
  }

  public static TextRange range(Node node) {
    return range(node.getStartMark(), node.getEndMark());
  }

  private static TextRange range(Optional<Mark> startMark, Optional<Mark> endMark) {
    if (startMark.isEmpty()) {
      throw new ParseException("Nodes are expected to have a start mark during conversion", null, null);
    }

    int startLine = startMark.get().getLine() + 1;
    int startColumn = startMark.get().getColumn();

    // endMark is not present. This happens for example when we have a file with only a comment.
    // in that case, the root node will be an empty MappingNode with only a startMark to which the comment is attached
    return endMark.map(mark -> TextRanges.range(startLine, startColumn, mark.getLine() + 1, mark.getColumn()))
            .orElseGet(() -> TextRanges.range(startLine, startColumn, startLine, startColumn));
  }

  public static String tag(Node node) {
    return node.getTag().getValue();
  }
}
