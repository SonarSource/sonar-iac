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

import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.tree.impl.MappingTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.SequenceTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.ScalarTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.TupleTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class CloudformationConverter {
  private static final Map<Class, Function<Node, Tree>> converters = new HashMap<>();
  static {
    converters.put(MappingNode.class, CloudformationConverter::convertMapping);
    converters.put(ScalarNode.class, CloudformationConverter::convertScalar);
    converters.put(SequenceNode.class, CloudformationConverter::convertSequence);
  }

  private CloudformationConverter() {}

  public static Tree convert(Node node) {
    return converters.get(node.getClass()).apply(node);
  }

  private static TupleTree convertTuple(NodeTuple tuple) {
    return new TupleTreeImpl(CloudformationConverter.convert(tuple.getKeyNode()), CloudformationConverter.convert(tuple.getValueNode()));
  }

  private static Tree convertMapping(Node node) {
    MappingNode mappingNode = (MappingNode) node;
    List<TupleTree> elements = new ArrayList<>();

    for (NodeTuple elementNode : mappingNode.getValue()) {
      elements.add(CloudformationConverter.convertTuple(elementNode));
    }

    return new MappingTreeImpl(elements, node.getTag().getValue());
  }

  private static Tree convertScalar(Node node) {
    return new ScalarTreeImpl((ScalarNode) node, scalarStyleConvert(((ScalarNode) node).getScalarStyle()));
  }

  private static Tree convertSequence(Node node) {
    SequenceNode sequenceNode = (SequenceNode) node;
    List<Tree> elements = new ArrayList<>();

    for (Node elementNode : sequenceNode.getValue()) {
      elements.add(CloudformationConverter.convert(elementNode));
    }

    return new SequenceTreeImpl(elements, node.getTag().getValue());
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
