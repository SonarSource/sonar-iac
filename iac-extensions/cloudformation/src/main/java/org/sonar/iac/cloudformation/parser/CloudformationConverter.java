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
package org.sonar.iac.cloudformation.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.snakeyaml.engine.v2.nodes.Tag;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.FunctionCallTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.tree.impl.FileTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.FunctionCallTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.MappingTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.ScalarTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.SequenceTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.TupleTreeImpl;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.ParseException;

class CloudformationConverter {
  private static final Map<Class<?>, Function<Node, CloudformationTree>> converters = new HashMap<>();
  static {
    converters.put(MappingNode.class, CloudformationConverter::convertMapping);
    converters.put(ScalarNode.class, CloudformationConverter::convertScalar);
    converters.put(SequenceNode.class, CloudformationConverter::convertSequence);
  }

  private CloudformationConverter() {}

  public static FileTree convertFile(List<Node> nodes) {
    if (nodes.isEmpty()) {
      throw new ParseException("Unexpected empty nodes list while converting file", null);
    }

    return new FileTreeImpl(convert(nodes.get(0)), range(nodes.get(0)));
  }

  public static CloudformationTree convert(Node node) {
    if (node.isRecursive()) {
      throw new ParserException("Recursive node found", node.getStartMark());
    }
    return converters.get(node.getClass()).apply(node);
  }

  private static TupleTree convertTuple(NodeTuple tuple) {
    CloudformationTree key = convert(tuple.getKeyNode());
    CloudformationTree value = convert(tuple.getValueNode());
    return new TupleTreeImpl(key, value, TextRanges.merge(Arrays.asList(key.textRange(), value.textRange())));
  }

  private static CloudformationTree convertMapping(Node node) {
    MappingNode mappingNode = (MappingNode) node;
    List<TupleTree> elements = new ArrayList<>();

    if (mappingNode.getValue().size() == 1 && isFullFunctionCall(mappingNode.getValue().get(0))) {
      NodeTuple functionCall = mappingNode.getValue().get(0);
      return convertTupleToFunctionCall((ScalarNode) functionCall.getKeyNode(), functionCall.getValueNode());
    }

    for (NodeTuple elementNode : mappingNode.getValue()) {
      elements.add(CloudformationConverter.convertTuple(elementNode));
    }

    return new MappingTreeImpl(elements, tag(node), range(node), comments(node));
  }

  private static CloudformationTree convertTupleToFunctionCall(ScalarNode functionNameNode, Node argumentList) {

    // Remove leading Fn:: from function name
    String functionName = functionNameNode.getValue().substring(4);
    TextRange functionRange = TextRanges.merge(List.of(range(functionNameNode), range(argumentList)));
    List<Comment> functionComments = comments(functionNameNode);
    functionComments.addAll(comments(argumentList));

    List<CloudformationTree> arguments = new ArrayList<>();
    if (argumentList instanceof SequenceNode) {
      for (Node elementNode : ((SequenceNode) argumentList).getValue()) {
        arguments.add(CloudformationConverter.convert(elementNode));
      }
    } else {
      arguments.add(CloudformationConverter.convert(argumentList));
    }

    return new FunctionCallTreeImpl(functionName, FunctionCallTree.Style.FULL, arguments, functionRange, functionComments);
  }

  private static boolean isFullFunctionCall(NodeTuple nodeTuple) {
    return nodeTuple.getKeyNode() instanceof ScalarNode && ((ScalarNode) nodeTuple.getKeyNode()).getValue().startsWith("Fn::");
  }

  private static boolean isShortFunctionCall(Node node) {
    return tag(node).startsWith("!");
  }

  private static CloudformationTree convertScalar(Node node) {
    ScalarNode scalarNode = (ScalarNode) node;
    if (isShortFunctionCall(scalarNode)) {
      return convertScalarToFunctionCall(scalarNode);
    }
    return new ScalarTreeImpl(scalarNode.getValue(), scalarStyleConvert(scalarNode.getScalarStyle()), tag(scalarNode), range(scalarNode), comments(node));
  }

  private static CloudformationTree convertSequence(Node node) {
    SequenceNode sequenceNode = (SequenceNode) node;

    List<CloudformationTree> elements = new ArrayList<>();

    for (Node elementNode : sequenceNode.getValue()) {
      elements.add(CloudformationConverter.convert(elementNode));
    }

    if (isShortFunctionCall(sequenceNode))  {
      return convertSequenceToFunctionCall(sequenceNode, elements);
    }
    return new SequenceTreeImpl(elements, tag(node), range(node), comments(node));
  }

  private static FunctionCallTree convertSequenceToFunctionCall(SequenceNode functionNode, List<CloudformationTree> arguments) {
    return new FunctionCallTreeImpl(shortStyleFunctionName(functionNode), FunctionCallTree.Style.SHORT, arguments, range(functionNode), comments(functionNode));
  }

  private static FunctionCallTree convertScalarToFunctionCall(ScalarNode scalarNode) {
    TextRange functionRange = range(scalarNode);
    ScalarTree argument = new ScalarTreeImpl(scalarNode.getValue(), ScalarTree.Style.OTHER, Tag.STR.getValue(), functionRange, Collections.emptyList());
    return new FunctionCallTreeImpl(shortStyleFunctionName(scalarNode), FunctionCallTree.Style.SHORT, List.of(argument), functionRange, comments(scalarNode));
  }

  /**
   * Remove leading exclamation mark from function name
   */
  private static String shortStyleFunctionName(Node functionNode) {
    return tag(functionNode).substring(1);
  }

  private static TextRange range(Node node) {
    return range(node.getStartMark(), node.getEndMark());
  }

  private static TextRange range(Optional<Mark> startMark, Optional<Mark> endMark) {
    if (!startMark.isPresent()) {
      throw new ParseException("Nodes are expected to have a start mark during conversion", null);
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
    // We prefix the comment value with # as it is already stripped away when arrive at this point.
    return new CommentImpl('#' + comment.getValue(), comment.getValue(), range(comment.getStartMark(), comment.getEndMark()));
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
