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
package org.sonar.iac.common.yaml;

import java.util.List;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.sonar.iac.common.api.tree.Tree;

public interface IacYamlConverter<T extends Tree, S> {

  default S convert(Node node) {
    if (node.isRecursive()) {
      throw new ParserException("Recursive node found", node.getStartMark());
    }
    if (node instanceof MappingNode mappingNode) {
      return convertMapping(mappingNode);
    } else if (node instanceof ScalarNode scalarNode) {
      return convertScalar(scalarNode);
    } else if (node instanceof SequenceNode sequenceNode) {
      return convertSequence(sequenceNode);
    } else {
      throw new ParserException("Unexpected node type: " + node.getClass(), node.getStartMark());
    }
  }

  T convertFile(List<Node> nodes);

  S convertMapping(MappingNode mappingNode);

  S convertScalar(ScalarNode scalarNode);

  S convertTuple(NodeTuple tuple);

  S convertSequence(SequenceNode sequenceNode);

}
