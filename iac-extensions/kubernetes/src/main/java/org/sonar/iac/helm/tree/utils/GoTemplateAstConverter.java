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

import com.google.protobuf.Any;
import com.google.protobuf.AnyOrBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.helm.tree.api.Node;

public final class GoTemplateAstConverter {
  private static final Logger LOG = LoggerFactory.getLogger(GoTemplateAstConverter.class);

  private static final Map<String, Converter<? extends Message, ? extends MessageOrBuilder>> typeNameToConverter = new HashMap<>();

  static {
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.ActionNode", new Converter<>(org.sonar.iac.helm.protobuf.ActionNode.class, org.sonar.iac.helm.tree.ActionNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.BoolNode", new Converter<>(org.sonar.iac.helm.protobuf.BoolNode.class, org.sonar.iac.helm.tree.BoolNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.BreakNode", new Converter<>(org.sonar.iac.helm.protobuf.BreakNode.class, org.sonar.iac.helm.tree.BreakNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.ChainNode", new Converter<>(org.sonar.iac.helm.protobuf.ChainNode.class, org.sonar.iac.helm.tree.ChainNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.CommandNode", new Converter<>(org.sonar.iac.helm.protobuf.CommandNode.class, org.sonar.iac.helm.tree.CommandNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.ContinueNode",
      new Converter<>(org.sonar.iac.helm.protobuf.ContinueNode.class, org.sonar.iac.helm.tree.ContinueNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.DotNode", new Converter<>(org.sonar.iac.helm.protobuf.DotNode.class, org.sonar.iac.helm.tree.DotNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.FieldNode", new Converter<>(org.sonar.iac.helm.protobuf.FieldNode.class, org.sonar.iac.helm.tree.FieldNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.IdentifierNode",
      new Converter<>(org.sonar.iac.helm.protobuf.IdentifierNode.class, org.sonar.iac.helm.tree.IdentifierNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.IfNode", new Converter<>(org.sonar.iac.helm.protobuf.IfNode.class, org.sonar.iac.helm.tree.IfNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.ListNode", new Converter<>(org.sonar.iac.helm.protobuf.ListNode.class, org.sonar.iac.helm.tree.ListNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.NilNode", new Converter<>(org.sonar.iac.helm.protobuf.NilNode.class, org.sonar.iac.helm.tree.NilNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.NumberNode", new Converter<>(org.sonar.iac.helm.protobuf.NumberNode.class, org.sonar.iac.helm.tree.NumberNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.PipeNode", new Converter<>(org.sonar.iac.helm.protobuf.PipeNode.class, org.sonar.iac.helm.tree.PipeNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.RangeNode", new Converter<>(org.sonar.iac.helm.protobuf.RangeNode.class, org.sonar.iac.helm.tree.RangeNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.StringNode", new Converter<>(org.sonar.iac.helm.protobuf.StringNode.class, org.sonar.iac.helm.tree.StringNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.TemplateNode",
      new Converter<>(org.sonar.iac.helm.protobuf.TemplateNode.class, org.sonar.iac.helm.tree.TemplateNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.TextNode", new Converter<>(org.sonar.iac.helm.protobuf.TextNode.class, org.sonar.iac.helm.tree.TextNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.VariableNode",
      new Converter<>(org.sonar.iac.helm.protobuf.VariableNode.class, org.sonar.iac.helm.tree.VariableNode::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.WithNode", new Converter<>(org.sonar.iac.helm.protobuf.WithNode.class, org.sonar.iac.helm.tree.WithNode::fromPb));
  }

  private GoTemplateAstConverter() {
    // utility class
  }

  @CheckForNull
  public static Node unpackNode(Any nodePb) {
    try {
      var typeName = typeName(nodePb);
      var converter = typeNameToConverter.get(typeName);
      if (converter == null) {
        LOG.debug("Unknown node type: {}", typeName);
        return null;
      }
      return converter.convert(nodePb);
    } catch (InvalidProtocolBufferException e) {
      LOG.debug("Failed to unpack node", e);
      return null;
    }
  }

  public static List<Node> unpack(Collection<Any> nodesPb) {
    return nodesPb.stream()
      .map(GoTemplateAstConverter::unpackNode)
      .collect(Collectors.toList());
  }

  private static String typeName(AnyOrBuilder nodePb) {
    return nodePb.getTypeUrl().substring(nodePb.getTypeUrl().lastIndexOf('/') + 1);
  }

  private static class Converter<M extends Message, T extends MessageOrBuilder> implements AnyToNodeConverter {
    private final Class<M> messageType;
    private final Function<T, Node> fromPb;

    public Converter(Class<M> messageType, Function<T, Node> fromPb) {
      this.messageType = messageType;
      this.fromPb = fromPb;
    }

    @Override
    public Node convert(Any any) throws InvalidProtocolBufferException {
      return fromPb.apply(
        (T) any.unpack(messageType));
    }
  }
}
