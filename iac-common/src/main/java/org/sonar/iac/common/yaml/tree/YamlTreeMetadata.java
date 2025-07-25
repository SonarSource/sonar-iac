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
package org.sonar.iac.common.yaml.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.Node;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.ParseException;

public record YamlTreeMetadata(String tag, TextRange textRange, int startPointer, int endPointer, List<Comment> comments) implements HasTextRange {

  /**
   *
   * @param tag
   * @param node
   * @return
   */
  @Deprecated
  public static YamlTreeMetadata fromNode(String tag, Node node) {
    return builder()
      .fromNode(node)
      .withTag(tag)
      .build();
  }

  @Deprecated
  public static YamlTreeMetadata fromNode(Node node) {
    return builder()
      .fromNode(node)
      .build();
  }

  @Deprecated
  public static YamlTreeMetadata fromNodes(String tag, Node firstNode, Node secondNode) {
    return builder()
      .fromNodes(firstNode, secondNode)
      .withTag(tag)
      .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Node startNode;
    private Node endNode;
    private String tag;
    private List<Comment> comments;

    public Builder fromNode(Node node) {
      this.startNode = node;
      this.endNode = node;
      return this;
    }

    public Builder fromNodes(Node startNode, Node endNode) {
      this.startNode = startNode;
      this.endNode = endNode;
      return this;
    }

    public Builder withTag(String tag) {
      this.tag = tag;
      return this;
    }

    public Builder withComments(List<CommentLine> commentLines) {
      this.comments = comments(commentLines);
      return this;
    }

    public YamlTreeMetadata build() {
      if (tag == null) {
        tag = tag(startNode);
      }
      if (comments == null) {
        comments = comments(startNode);
      }

      var range = TextRanges.merge(range(startNode), range(endNode));
      var startPointer = pointer(startNode.getStartMark().orElse(null));
      var endPointer = startPointer;
      if (endNode.getEndMark().isPresent()) {
        endPointer = pointer(endNode.getEndMark().orElse(null));
      }
      return new YamlTreeMetadata(tag, range, startPointer, endPointer, comments);
    }

    public static String tag(Node node) {
      return node.getTag().getValue();
    }

    private static int pointer(@Nullable Mark mark) {
      if (mark == null) {
        throw new ParseException("Nodes are expected to have a start mark during conversion", null, null);
      }
      // getIndex() returns the byte offset in the input stream, getPointer() sometimes returns a different value, making start pointer greater
      // than end pointer
      return mark.getIndex();
    }

    public static TextRange range(Node node) {
      return range(node.getStartMark().orElse(null), node.getEndMark().orElse(null));
    }

    private static TextRange range(@Nullable Mark startMark, @Nullable Mark endMark) {
      if (startMark == null) {
        throw new ParseException("Nodes are expected to have a start mark during conversion", null, null);
      }

      int startLine = startMark.getLine() + 1;
      int startColumn = startMark.getColumn();

      // endMark is not present. This happens for example when we have a file with only a comment.
      // in that case, the root node will be an empty MappingNode with only a startMark to which the comment is attached
      if (endMark != null) {
        return TextRanges.range(startLine, startColumn, endMark.getLine() + 1, endMark.getColumn());
      } else {
        return TextRanges.range(startLine, startColumn, startLine, startColumn);
      }
    }

    public static List<Comment> comments(Node node) {
      // For now we group all comments together. This might change, once we have a reason to separate them.
      List<Comment> comments = new ArrayList<>(comments(node.getBlockComments()));
      comments.addAll(comments(node.getInLineComments()));
      comments.addAll(comments(node.getEndComments()));
      return comments;
    }

    private static List<Comment> comments(@Nullable List<CommentLine> commentLines) {
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
      var range = range(comment.getStartMark().orElse(null), comment.getEndMark().orElse(null));
      return new CommentImpl('#' + comment.getValue(), comment.getValue(), range);
    }
  }
}
