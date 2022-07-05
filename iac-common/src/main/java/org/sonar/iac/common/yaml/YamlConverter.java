/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.common.yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.FileTreeImpl;
import org.sonar.iac.common.yaml.tree.MappingTreeImpl;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;
import org.sonar.iac.common.yaml.tree.SequenceTreeImpl;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.TupleTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class YamlConverter {

  private final Map<Class<?>, Function<Node, YamlTree>> converters = Map.of(
    MappingNode.class, node -> convertMapping((MappingNode) node),
    ScalarNode.class, node -> convertScalar((ScalarNode) node),
    SequenceNode.class, node -> convertSequence((SequenceNode) node)
  );

  public YamlTree convert(Node node) {
    if (node.isRecursive()) {
      throw new ParserException("Recursive node found", node.getStartMark());
    }
    return converters.get(node.getClass()).apply(node);
  }

  public FileTree convertFile(List<Node> nodes) {
    if (nodes.isEmpty()) {
      throw new ParseException("Unexpected empty nodes list while converting file", null);
    }

    return new FileTreeImpl(convert(nodes.get(0)), range(nodes.get(0)));
  }

  protected YamlTree convertMapping(MappingNode mappingNode) {
    List<TupleTree> elements = new ArrayList<>();

    for (NodeTuple elementNode : mappingNode.getValue()) {
      elements.add(convertTuple(elementNode));
    }

    return new MappingTreeImpl(elements, tag(mappingNode), range(mappingNode), comments(mappingNode));
  }

  protected YamlTree convertScalar(ScalarNode scalarNode) {
    return new ScalarTreeImpl(scalarNode.getValue(), scalarStyleConvert(scalarNode.getScalarStyle()), tag(scalarNode), range(scalarNode), comments(scalarNode));
  }

  protected TupleTree convertTuple(NodeTuple tuple) {
    YamlTree key = convert(tuple.getKeyNode());
    YamlTree value = convert(tuple.getValueNode());
    return new TupleTreeImpl(key, value, TextRanges.merge(Arrays.asList(key.textRange(), value.textRange())));
  }

  protected YamlTree convertSequence(SequenceNode sequenceNode) {
    List<YamlTree> elements = new ArrayList<>();

    for (Node elementNode : sequenceNode.getValue()) {
      elements.add(convert(elementNode));
    }
    return new SequenceTreeImpl(elements, tag(sequenceNode), range(sequenceNode), comments(sequenceNode));
  }

  protected static ScalarTree.Style scalarStyleConvert(ScalarStyle style) {
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

  protected static TextRange range(Node node) {
    return range(node.getStartMark(), node.getEndMark());
  }

  protected static TextRange range(Optional<Mark> startMark, Optional<Mark> endMark) {
    if (startMark.isEmpty()) {
      throw new ParseException("Nodes are expected to have a start mark during conversion", null);
    }

    return endMark.map(mark -> TextRanges.range(startMark.get().getLine() + 1, startMark.get().getColumn(), mark.getLine() + 1, mark.getColumn())).orElseGet(() -> TextRanges.range(startMark.get().getLine() + 1, startMark.get().getColumn(), startMark.get().getLine() + 1, startMark.get().getColumn()));

    // endMark is not present. This happens for example when we have a file with only a comment.
    // in that case, the root node will be an empty MappingNode with only a startMark to which the comment is attached
  }

  protected static String tag(Node node) {
    return node.getTag().getValue();
  }

  protected static List<Comment> comments(Node node) {
    // For now we group all comments together. This might change, once we have a reason to separate them.
    List<Comment> comments = new ArrayList<>(comments(node.getBlockComments()));
    comments.addAll(comments(node.getInLineComments()));
    comments.addAll(comments(node.getEndComments()));
    return comments;
  }

  protected static List<Comment> comments(@Nullable List<CommentLine> commentLines) {
    if (commentLines == null) {
      return Collections.emptyList();
    }
    List<Comment> comments = new ArrayList<>();
    for (CommentLine comment : commentLines) {
      comments.add(comment(comment));
    }
    return comments;
  }

  protected static Comment comment(CommentLine comment) {
    // We prefix the comment value with # as it is already stripped away when arrive at this point.
    return new CommentImpl('#' + comment.getValue(), comment.getValue(), range(comment.getStartMark(), comment.getEndMark()));
  }
}
