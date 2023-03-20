/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.sonar.iac.common.api.tree.impl.TextRange;
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
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;
import org.sonarsource.analyzer.commons.collections.ListUtils;

import static org.sonar.iac.common.yaml.tree.YamlTreeMetadata.range;

public class YamlConverter {

  private final Map<Class<?>, Function<Node, YamlTree>> converters = Map.of(
    MappingNode.class, node -> convertMapping((MappingNode) node),
    ScalarNode.class, node -> convertScalar((ScalarNode) node),
    SequenceNode.class, node -> convertSequence((SequenceNode) node));

  public YamlTree convert(Node node) {
    if (node.isRecursive()) {
      throw new ParserException("Recursive node found", node.getStartMark());
    }
    return converters.get(node.getClass()).apply(node);
  }

  public FileTree convertFile(List<Node> nodes) {
    if (nodes.isEmpty()) {
      throw new ParseException("Unexpected empty nodes list while converting file", null, null);
    }
    TextRange fileRange = TextRanges.merge(List.of(range(nodes.get(0)), range(ListUtils.getLast(nodes))));
    YamlTreeMetadata metadata = new YamlTreeMetadata("FILE", fileRange, Collections.emptyList());
    List<YamlTree> documents = nodes.stream().map(this::convert).collect(Collectors.toList());
    return new FileTreeImpl(documents, metadata);
  }

  protected YamlTree convertMapping(MappingNode mappingNode) {
    List<TupleTree> elements = new ArrayList<>();

    for (NodeTuple elementNode : mappingNode.getValue()) {
      elements.add(convertTuple(elementNode));
    }

    return new MappingTreeImpl(elements, YamlTreeMetadata.fromNode(mappingNode));
  }

  protected YamlTree convertScalar(ScalarNode scalarNode) {
    return new ScalarTreeImpl(scalarNode.getValue(), scalarStyleConvert(scalarNode.getScalarStyle()), YamlTreeMetadata.fromNode(scalarNode));
  }

  protected TupleTree convertTuple(NodeTuple tuple) {
    YamlTree key = convert(tuple.getKeyNode());
    YamlTree value = convert(tuple.getValueNode());
    return new TupleTreeImpl(key, value, YamlTreeMetadata.fromNodes("TUPLE", tuple.getKeyNode(), tuple.getValueNode()));
  }

  protected YamlTree convertSequence(SequenceNode sequenceNode) {
    List<YamlTree> elements = new ArrayList<>();

    for (Node elementNode : sequenceNode.getValue()) {
      elements.add(convert(elementNode));
    }
    return new SequenceTreeImpl(elements, YamlTreeMetadata.fromNode(sequenceNode));
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

}
