/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.cloudformation.parser;

import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.tree.impl.FileTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.MappingTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.SequenceTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.ScalarTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.TupleTreeImpl;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

class CloudformationConverter {
  private static final Map<Class<?>, Function<Node, Tree>> converters = new HashMap<>();
  static {
    converters.put(MappingNode.class, CloudformationConverter::convertMapping);
    converters.put(ScalarNode.class, CloudformationConverter::convertScalar);
    converters.put(SequenceNode.class, CloudformationConverter::convertSequence);
  }

  private CloudformationConverter() {}

  public static FileTree convertFile(List<Node> nodes) {
    if (!nodes.isEmpty()) {
      return new FileTreeImpl(convert(nodes.get(0)), range(nodes.get(0)));
    }

    // empty file
    return new FileTreeImpl(null, TextRanges.range(0, 0, 0, 0));
  }

  public static Tree convert(Node node) {
    return converters.get(node.getClass()).apply(node);
  }

  private static TupleTree convertTuple(NodeTuple tuple) {
    Tree key = convert(tuple.getKeyNode());
    Tree value = convert(tuple.getValueNode());
    return new TupleTreeImpl(key, value, TextRanges.merge(Arrays.asList(key.textRange(), value.textRange())));
  }

  private static Tree convertMapping(Node node) {
    MappingNode mappingNode = (MappingNode) node;
    List<TupleTree> elements = new ArrayList<>();

    for (NodeTuple elementNode : mappingNode.getValue()) {
      elements.add(CloudformationConverter.convertTuple(elementNode));
    }

    return new MappingTreeImpl(elements, tag(node), range(node), comments(node));
  }

  private static Tree convertScalar(Node node) {
    ScalarNode scalarNode = (ScalarNode) node;
    return new ScalarTreeImpl(scalarNode.getValue(), scalarStyleConvert(scalarNode.getScalarStyle()), tag(scalarNode), range(scalarNode), comments(node));
  }

  private static Tree convertSequence(Node node) {
    SequenceNode sequenceNode = (SequenceNode) node;
    List<Tree> elements = new ArrayList<>();

    for (Node elementNode : sequenceNode.getValue()) {
      elements.add(CloudformationConverter.convert(elementNode));
    }

    return new SequenceTreeImpl(elements, tag(node), range(node), comments(node));
  }

  private static TextRange range(Node node) {
    return range(node.getStartMark(), node.getEndMark());
  }

  private static TextRange range(Optional<Mark> startMark, Optional<Mark> endMark) {
    if (!startMark.isPresent()) {
      throw new IllegalArgumentException("Nodes are expected to have a start mark during conversion");
    }

    if (endMark.isPresent()) {
      return TextRanges.range(startMark.get().getLine() + 1, startMark.get().getColumn(), endMark.get().getLine() + 1, endMark.get().getColumn());
    }

    // endMark is not present. This happens for example when we have a file with only a comment.
    // in that case, the root node will be an empty MappingNode with only a startMark to which the comment is attached
    return TextRanges.range(startMark.get().getLine() + 1, startMark.get().getColumn(), startMark.get().getLine() + 1, startMark.get().getColumn());
  }

  private static String tag(Node node) {
    return node.getTag().getValue();
  }

  private static List<Comment> comments(Node node) {
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
    return new CommentImpl(comment.getValue(), comment.getValue(), range(comment.getStartMark(), comment.getEndMark()));
  }

  private static ScalarTree.Style scalarStyleConvert(ScalarStyle style) {
    switch (style) {
      case DOUBLE_QUOTED:
        return ScalarTree.Style.DOUBLE_QUOTED;
      case SINGLE_QUOTED:
        return ScalarTree.Style.SINGLE_QUOTED;
      case LITERAL:
        return ScalarTree.Style.LITERAL;
      case FOLDED:
        return ScalarTree.Style.FOLDED;
      case PLAIN:
        return ScalarTree.Style.PLAIN;
      default:
        return ScalarTree.Style.OTHER;
    }
  }
}
