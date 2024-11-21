/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.yaml;

import java.util.ArrayList;
import java.util.List;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
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

public class YamlConverter implements IacYamlConverter<FileTree, YamlTree> {

  @Override
  public FileTree convertFile(List<Node> nodes) {
    if (nodes.isEmpty()) {
      throw new ParseException("Unexpected empty nodes list while converting file", null, null);
    }

    var fileNode = nodes.get(0);
    var lastNode = ListUtils.getLast(nodes);
    var metadata = YamlTreeMetadata.builder()
      .fromNodes(fileNode, lastNode)
      .withTag("FILE")
      .withComments(fileNode.getEndComments())
      .build();
    List<YamlTree> documents = nodes.stream().map(this::convert).toList();
    return new FileTreeImpl(documents, metadata);
  }

  @Override
  public YamlTree convertMapping(MappingNode mappingNode) {
    List<TupleTree> elements = new ArrayList<>();

    for (NodeTuple elementNode : mappingNode.getValue()) {
      elements.add(convertTuple(elementNode));
    }

    return new MappingTreeImpl(elements, YamlTreeMetadata.fromNode(mappingNode));
  }

  @Override
  public YamlTree convertScalar(ScalarNode scalarNode) {
    return new ScalarTreeImpl(scalarNode.getValue(), scalarStyleConvert(scalarNode.getScalarStyle()),
      YamlTreeMetadata.fromNode(scalarNode));
  }

  @Override
  public TupleTree convertTuple(NodeTuple tuple) {
    YamlTree key = convert(tuple.getKeyNode());
    YamlTree value = convert(tuple.getValueNode());
    return new TupleTreeImpl(key, value, YamlTreeMetadata.fromNodes("TUPLE", tuple.getKeyNode(), tuple.getValueNode()));
  }

  @Override
  public YamlTree convertSequence(SequenceNode sequenceNode) {
    List<YamlTree> elements = new ArrayList<>();

    for (Node elementNode : sequenceNode.getValue()) {
      elements.add(convert(elementNode));
    }
    return new SequenceTreeImpl(elements, YamlTreeMetadata.fromNode(sequenceNode));
  }

  protected static ScalarTree.Style scalarStyleConvert(ScalarStyle style) {
    return switch (style) {
      case DOUBLE_QUOTED -> ScalarTree.Style.DOUBLE_QUOTED;
      case SINGLE_QUOTED -> ScalarTree.Style.SINGLE_QUOTED;
      case LITERAL -> ScalarTree.Style.LITERAL;
      case FOLDED -> ScalarTree.Style.FOLDED;
      case PLAIN -> ScalarTree.Style.PLAIN;
      // As of version 2.8, JSON_SCALAR_STYLE is only used when serializing as JSON
      case JSON_SCALAR_STYLE -> ScalarTree.Style.PLAIN;
    };
  }

}
