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
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.helm.tree.api.Location;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.impl.LocationImpl;

public class GoTemplateAstHelper {

  private GoTemplateAstHelper() {
    // utility class
  }

  public static List<ValuePath> findNodes(GoTemplateTree tree, TextRange range, String text) {
    var location = LocationImpl.toPositionAndLength(range, text);
    var nodes = tree.root().children().stream()
      .filter(hasOverlayingLocation(location))
      .collect(Collectors.toList());

    return allChildren(nodes).stream()
      .filter(FieldNode.class::isInstance)
      .map(FieldNode.class::cast)
      .map(FieldNode::identifiers)
      .map(ValuePath::new)
      .collect(Collectors.toList());
  }

  private static Predicate<Node> hasOverlayingLocation(Location location) {
    return node -> {
      var position = location.position();
      var length = location.length();
      var nodePosition = node.location().position();
      var nodeLength = node.location().length();
      return (nodePosition >= position && nodePosition <= position + length) ||
        (nodePosition + nodeLength >= position && nodePosition + nodeLength <= position + length) ||
        (nodePosition < position && nodePosition + nodeLength > position + length);
    };
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
