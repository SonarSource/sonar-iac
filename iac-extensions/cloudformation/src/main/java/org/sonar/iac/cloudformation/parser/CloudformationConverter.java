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
package org.sonar.iac.cloudformation.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.sonar.iac.cloudformation.tree.FunctionCallTree;
import org.sonar.iac.cloudformation.tree.FunctionCallTreeImpl;
import org.sonar.iac.common.yaml.YamlConverter;
import org.sonar.iac.common.yaml.tree.MappingTreeImpl;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;
import org.sonar.iac.common.yaml.tree.SequenceTreeImpl;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

import static org.sonar.iac.common.yaml.tree.YamlTreeMetadata.Builder.tag;

public class CloudformationConverter extends YamlConverter {

  @Override
  public YamlTree convertMapping(MappingNode mappingNode) {
    List<TupleTree> elements = new ArrayList<>();

    if (mappingNode.getValue().size() == 1 && mappingNode.getValue().get(0).getKeyNode() instanceof ScalarNode) {
      NodeTuple functionCallNode = mappingNode.getValue().get(0);
      FunctionCallTree functionCall = convertTupleToFunctionCall((ScalarNode) functionCallNode.getKeyNode(), functionCallNode.getValueNode());
      if (functionCall != null) {
        return functionCall;
      }
    }

    for (NodeTuple elementNode : mappingNode.getValue()) {
      elements.add(convertTuple(elementNode));
    }

    return new MappingTreeImpl(elements, YamlTreeMetadata.fromNode(mappingNode));
  }

  @Nullable
  private FunctionCallTreeImpl convertTupleToFunctionCall(ScalarNode functionNameNode, Node argumentList) {
    return fullStyleFunctionName(functionNameNode.getValue()).map(functionName -> {
      List<YamlTree> arguments = new ArrayList<>();
      if (argumentList instanceof SequenceNode) {
        for (Node elementNode : ((SequenceNode) argumentList).getValue()) {
          arguments.add(convert(elementNode));
        }
      } else {
        arguments.add(convert(argumentList));
      }
      return new FunctionCallTreeImpl(functionName, FunctionCallTree.Style.FULL, arguments, YamlTreeMetadata.fromNodes("FUNCTION_CALL", functionNameNode, argumentList));
    }).orElse(null);
  }

  @Override
  public YamlTree convertScalar(ScalarNode scalarNode) {
    FunctionCallTree functionCallFromScalar = convertScalarToFunctionCall(scalarNode);
    if (functionCallFromScalar != null) {
      return functionCallFromScalar;
    }
    return new ScalarTreeImpl(scalarNode.getValue(), scalarStyleConvert(scalarNode.getScalarStyle()), YamlTreeMetadata.fromNode(scalarNode));
  }

  @Override
  public YamlTree convertSequence(SequenceNode sequenceNode) {
    List<YamlTree> elements = new ArrayList<>();

    for (Node elementNode : sequenceNode.getValue()) {
      elements.add(convert(elementNode));
    }

    FunctionCallTree functionCallFromScalar = convertSequenceToFunctionCall(sequenceNode, elements);
    if (functionCallFromScalar != null) {
      return functionCallFromScalar;
    }
    return new SequenceTreeImpl(elements, YamlTreeMetadata.fromNode(sequenceNode));
  }

  @Nullable
  private static FunctionCallTree convertSequenceToFunctionCall(SequenceNode functionNode, List<YamlTree> arguments) {
    return shortStyleFunctionName(functionNode)
      .map(functionName -> new FunctionCallTreeImpl(functionName, FunctionCallTree.Style.SHORT, arguments, YamlTreeMetadata.fromNode("FUNCTION_CALL", functionNode))).orElse(null);
  }

  @Nullable
  private static FunctionCallTree convertScalarToFunctionCall(ScalarNode scalarNode) {
    return shortStyleFunctionName(scalarNode).map(functionName -> {
      var argument = new ScalarTreeImpl(scalarNode.getValue(), ScalarTree.Style.OTHER, YamlTreeMetadata.fromNode(Tag.STR.getValue(), scalarNode));
      return new FunctionCallTreeImpl(functionName, FunctionCallTree.Style.SHORT, List.of(argument), YamlTreeMetadata.fromNode("FUNCTION_CALL", scalarNode));
    }).orElse(null);
  }

  private static Optional<String> shortStyleFunctionName(Node functionNode) {
    String tag = tag(functionNode);
    if (tag.startsWith("!")) {
      return Optional.of(tag.substring(1));
    }
    return Optional.empty();
  }

  private static Optional<String> fullStyleFunctionName(String value) {
    if (value.startsWith("Fn::")) {
      return Optional.of(value.substring(4));
    } else if (value.equals("Ref")) {
      return Optional.of(value);
    }
    return Optional.empty();
  }
}
