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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.impl.ActionNodeImpl;
import org.sonar.iac.helm.tree.impl.BoolNodeImpl;
import org.sonar.iac.helm.tree.impl.BreakNodeImpl;
import org.sonar.iac.helm.tree.impl.ChainNodeImpl;
import org.sonar.iac.helm.tree.impl.CommandNodeImpl;
import org.sonar.iac.helm.tree.impl.ContinueNodeImpl;
import org.sonar.iac.helm.tree.impl.DotNodeImpl;
import org.sonar.iac.helm.tree.impl.FieldNodeImpl;
import org.sonar.iac.helm.tree.impl.IdentifierNodeImpl;
import org.sonar.iac.helm.tree.impl.IfNodeImpl;
import org.sonar.iac.helm.tree.impl.ListNodeImpl;
import org.sonar.iac.helm.tree.impl.NilNodeImpl;
import org.sonar.iac.helm.tree.impl.NumberNodeImpl;
import org.sonar.iac.helm.tree.impl.PipeNodeImpl;
import org.sonar.iac.helm.tree.impl.RangeNodeImpl;
import org.sonar.iac.helm.tree.impl.StringNodeImpl;
import org.sonar.iac.helm.tree.impl.TemplateNodeImpl;
import org.sonar.iac.helm.tree.impl.TextNodeImpl;
import org.sonar.iac.helm.tree.impl.VariableNodeImpl;
import org.sonar.iac.helm.tree.impl.WithNodeImpl;

import javax.annotation.CheckForNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class GoTemplateAstConverter {
  private static final Logger LOG = LoggerFactory.getLogger(GoTemplateAstConverter.class);

  private static final Map<String, Converter<? extends Message, ? extends MessageOrBuilder>> typeNameToConverter = new HashMap<>();

  static {
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.ActionNode", new Converter<>(org.sonar.iac.helm.protobuf.ActionNode.class, ActionNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.BoolNode", new Converter<>(org.sonar.iac.helm.protobuf.BoolNode.class, BoolNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.BreakNode", new Converter<>(org.sonar.iac.helm.protobuf.BreakNode.class, BreakNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.ChainNode", new Converter<>(org.sonar.iac.helm.protobuf.ChainNode.class, ChainNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.CommandNode", new Converter<>(org.sonar.iac.helm.protobuf.CommandNode.class, CommandNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.ContinueNode",
      new Converter<>(org.sonar.iac.helm.protobuf.ContinueNode.class, ContinueNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.DotNode", new Converter<>(org.sonar.iac.helm.protobuf.DotNode.class, DotNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.FieldNode", new Converter<>(org.sonar.iac.helm.protobuf.FieldNode.class, FieldNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.IdentifierNode",
      new Converter<>(org.sonar.iac.helm.protobuf.IdentifierNode.class, IdentifierNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.IfNode", new Converter<>(org.sonar.iac.helm.protobuf.IfNode.class, IfNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.ListNode", new Converter<>(org.sonar.iac.helm.protobuf.ListNode.class, ListNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.NilNode", new Converter<>(org.sonar.iac.helm.protobuf.NilNode.class, NilNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.NumberNode", new Converter<>(org.sonar.iac.helm.protobuf.NumberNode.class, NumberNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.PipeNode", new Converter<>(org.sonar.iac.helm.protobuf.PipeNode.class, PipeNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.RangeNode", new Converter<>(org.sonar.iac.helm.protobuf.RangeNode.class, RangeNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.StringNode", new Converter<>(org.sonar.iac.helm.protobuf.StringNode.class, StringNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.TemplateNode",
      new Converter<>(org.sonar.iac.helm.protobuf.TemplateNode.class, TemplateNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.TextNode", new Converter<>(org.sonar.iac.helm.protobuf.TextNode.class, TextNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.VariableNode",
      new Converter<>(org.sonar.iac.helm.protobuf.VariableNode.class, VariableNodeImpl::fromPb));
    typeNameToConverter.put("org.sonar.iac.helm.protobuf.WithNode", new Converter<>(org.sonar.iac.helm.protobuf.WithNode.class, WithNodeImpl::fromPb));
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
      .toList();
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
      return fromPb.apply((T) any.unpack(messageType));
    }
  }
}
