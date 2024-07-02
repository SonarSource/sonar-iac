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
package org.sonar.iac.helm.tree.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.helm.tree.api.IdentifierNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.NodeType;

public final class GoTemplateAstHelper {

  private GoTemplateAstHelper() {
    // utility class
  }

  public static Stream<Node> findNodesToHighlight(GoTemplateTree tree, TextRange range) {
    return Stream.concat(Stream.concat(
      findValuePathNodes(tree, range),
      findIncludeFunctionsFirstArg(tree, range)),
      findToYamlNodes(tree, range));
  }

  static Stream<FieldNode> findValuePathNodes(GoTemplateTree tree, TextRange textRange) {
    var nodes = tree.root().children().stream()
      .filter(node -> node.textRange().overlap(textRange))
      .toList();

    return nodesWithAllChildren(nodes)
      .filter(FieldNode.class::isInstance)
      // Sometimes top-level nodes have a very broad range and some children can be actually outside the range.
      .filter(node -> node.textRange().overlap(textRange))
      .map(FieldNode.class::cast);
  }

  public static List<ValuePath> findValuePaths(GoTemplateTree tree, TextRange range) {
    return findValuePathNodes(tree, range)
      .map(FieldNode::identifiers)
      .map(ValuePath::new)
      .toList();
  }

  private static Stream<Node> findIncludeFunctionsFirstArg(GoTemplateTree tree, TextRange textRange) {
    return collectNodesByType(tree.root(), NodeType.NODE_COMMAND, CommandNode.class)
      .filter(node -> node.textRange().overlap(textRange))
      .filter(node -> isFunction(node, "include"))
      .map(cmd -> cmd.arguments().get(1));
  }

  private static Stream<? extends Node> findToYamlNodes(GoTemplateTree tree, TextRange textRange) {
    return collectNodesByType(tree.root(), NodeType.NODE_COMMAND, CommandNode.class)
      .filter(node -> node.textRange().overlap(textRange))
      .filter(node -> isFunction(node, "toYaml"));
  }

  private static Stream<Tree> nodesWithAllChildren(List<Tree> nodes) {
    return Stream.concat(
      nodes.stream(),
      nodes.stream().flatMap(node -> nodesWithAllChildren(node.children())));
  }

  private static <T extends Node> Stream<T> collectNodesByType(Node node, NodeType type, Class<T> nodeClass) {
    return Stream.concat(
      Stream.of(node).filter(n -> type == n.type()),
      node.children().stream()
        .map(Node.class::cast)
        .flatMap(child -> collectNodesByType(child, type, nodeClass)))
      .map(nodeClass::cast);
  }

  public static void addChildrenIfPresent(Collection<Tree> children, @Nullable Node tree) {
    if (tree != null) {
      children.add(tree);
    }
  }

  private static boolean isFunction(CommandNode commandNode, String functionName) {
    return commandNode.arguments().size() > 1
      && commandNode.arguments().get(0) instanceof IdentifierNode identifierNode
      && functionName.equals(identifierNode.identifier());
  }
}
