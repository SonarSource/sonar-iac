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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.helm.tree.api.Node;

public class GoTemplateAstHelper {

  private GoTemplateAstHelper() {
    // utility class
  }

  public static List<ValuePath> findNodes(GoTemplateTree tree, TextRange range, String text) {
    var positionAndLength = TextRanges.toPositionAndLength(range, text);
    var position = positionAndLength.first();
    var length = positionAndLength.second();
    var nodes = tree.root().children().stream()
      .filter(hasOverlayingLocation(position, length))
      .collect(Collectors.toList());

    return allChildren(nodes).stream()
      .filter(FieldNode.class::isInstance)
      .map(FieldNode.class::cast)
      .map(FieldNode::identifiers)
      .map(ValuePath::new)
      .collect(Collectors.toList());
  }

  private static Predicate<Node> hasOverlayingLocation(Integer position, Integer length) {
    return node -> (node.position() >= position && node.position() <= position + length) ||
      (node.position() + 15 >= position && node.position() + 15 <= position + length) ||
      (node.position() < position && node.position() + 15 > position + length);
  }

  private static List<Node> allChildren(List<Node> nodes) {
    List<Node> allNodes = new ArrayList<>(nodes);
    for (int i = 0; i < allNodes.size(); i++) {
      allNodes.addAll(allNodes.get(i).children());
    }
    return allNodes;
  }

  public static void addChildrenIfPresent(List<Node> children, @Nullable Node tree) {
    if (tree != null) {
      children.add(tree);
    }
  }
}
