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
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageOrBuilder;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.helm.tree.Node;

public final class GoTemplateAstConverter {
  private static final Logger LOG = LoggerFactory.getLogger(GoTemplateAstConverter.class);
  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  private GoTemplateAstConverter() {
    // utility class
  }

  @CheckForNull
  public static Node unpackNode(Any nodePb) {
    try {
      var types = typesForMessage(nodePb);
      Class<? extends GeneratedMessageV3> messageClass = types.messageType;
      Class<? extends MessageOrBuilder> messageClassOrBuilder = types.messageOrBuilderType;
      Class<? extends Node> targetClass = types.nodeType;
      var handle = lookup.findStatic(targetClass, "fromPb", MethodType.methodType(Node.class, messageClassOrBuilder));
      return (Node) handle.invoke(nodePb.unpack(messageClass));
    } catch (Throwable t) {
      LOG.debug("Failed to unpack node", t);
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

  private static Types typesForMessage(AnyOrBuilder nodePb) {
    Types types;
    switch (typeName(nodePb)) {
      case "org.sonar.iac.helm.ActionNode":
        types = new Types(org.sonar.iac.helm.ActionNode.class, org.sonar.iac.helm.ActionNodeOrBuilder.class, org.sonar.iac.helm.tree.ActionNode.class);
        break;
      case "org.sonar.iac.helm.BoolNode":
        types = new Types(org.sonar.iac.helm.BoolNode.class, org.sonar.iac.helm.BoolNodeOrBuilder.class, org.sonar.iac.helm.tree.BoolNode.class);
        break;
      case "org.sonar.iac.helm.BreakNode":
        types = new Types(org.sonar.iac.helm.BreakNode.class, org.sonar.iac.helm.BreakNodeOrBuilder.class, org.sonar.iac.helm.tree.BreakNode.class);
        break;
      case "org.sonar.iac.helm.ChainNode":
        types = new Types(org.sonar.iac.helm.ChainNode.class, org.sonar.iac.helm.ChainNodeOrBuilder.class, org.sonar.iac.helm.tree.ChainNode.class);
        break;
      case "org.sonar.iac.helm.CommandNode":
        types = new Types(org.sonar.iac.helm.CommandNode.class, org.sonar.iac.helm.CommandNodeOrBuilder.class, org.sonar.iac.helm.tree.CommandNode.class);
        break;
      case "org.sonar.iac.helm.ContinueNode":
        types = new Types(org.sonar.iac.helm.ContinueNode.class, org.sonar.iac.helm.ContinueNodeOrBuilder.class, org.sonar.iac.helm.tree.ContinueNode.class);
        break;
      case "org.sonar.iac.helm.DotNode":
        types = new Types(org.sonar.iac.helm.DotNode.class, org.sonar.iac.helm.DotNodeOrBuilder.class, org.sonar.iac.helm.tree.DotNode.class);
        break;
      case "org.sonar.iac.helm.FieldNode":
        types = new Types(org.sonar.iac.helm.FieldNode.class, org.sonar.iac.helm.FieldNodeOrBuilder.class, org.sonar.iac.helm.tree.FieldNode.class);
        break;
      case "org.sonar.iac.helm.IdentifierNode":
        types = new Types(org.sonar.iac.helm.IdentifierNode.class, org.sonar.iac.helm.IdentifierNodeOrBuilder.class, org.sonar.iac.helm.tree.IdentifierNode.class);
        break;
      case "org.sonar.iac.helm.IfNode":
        types = new Types(org.sonar.iac.helm.IfNode.class, org.sonar.iac.helm.IfNodeOrBuilder.class, org.sonar.iac.helm.tree.IfNode.class);
        break;
      case "org.sonar.iac.helm.ListNode":
        types = new Types(org.sonar.iac.helm.ListNode.class, org.sonar.iac.helm.ListNodeOrBuilder.class, org.sonar.iac.helm.tree.ListNode.class);
        break;
      case "org.sonar.iac.helm.NilNode":
        types = new Types(org.sonar.iac.helm.NilNode.class, org.sonar.iac.helm.NilNodeOrBuilder.class, org.sonar.iac.helm.tree.NilNode.class);
        break;
      case "org.sonar.iac.helm.NumberNode":
        types = new Types(org.sonar.iac.helm.NumberNode.class, org.sonar.iac.helm.NumberNodeOrBuilder.class, org.sonar.iac.helm.tree.NumberNode.class);
        break;
      case "org.sonar.iac.helm.PipeNode":
        types = new Types(org.sonar.iac.helm.PipeNode.class, org.sonar.iac.helm.PipeNodeOrBuilder.class, org.sonar.iac.helm.tree.PipeNode.class);
        break;
      case "org.sonar.iac.helm.RangeNode":
        types = new Types(org.sonar.iac.helm.RangeNode.class, org.sonar.iac.helm.RangeNodeOrBuilder.class, org.sonar.iac.helm.tree.RangeNode.class);
        break;
      case "org.sonar.iac.helm.StringNode":
        types = new Types(org.sonar.iac.helm.StringNode.class, org.sonar.iac.helm.StringNodeOrBuilder.class, org.sonar.iac.helm.tree.StringNode.class);
        break;
      case "org.sonar.iac.helm.TemplateNode":
        types = new Types(org.sonar.iac.helm.TemplateNode.class, org.sonar.iac.helm.TemplateNodeOrBuilder.class, org.sonar.iac.helm.tree.TemplateNode.class);
        break;
      case "org.sonar.iac.helm.TextNode":
        types = new Types(org.sonar.iac.helm.TextNode.class, org.sonar.iac.helm.TextNodeOrBuilder.class, org.sonar.iac.helm.tree.TextNode.class);
        break;
      case "org.sonar.iac.helm.VariableNode":
        types = new Types(org.sonar.iac.helm.VariableNode.class, org.sonar.iac.helm.VariableNodeOrBuilder.class, org.sonar.iac.helm.tree.VariableNode.class);
        break;
      case "org.sonar.iac.helm.WithNode":
        types = new Types(org.sonar.iac.helm.WithNode.class, org.sonar.iac.helm.WithNodeOrBuilder.class, org.sonar.iac.helm.tree.WithNode.class);
        break;
      default:
        throw new IllegalArgumentException("Unknown message type: " + typeName(nodePb));
    }
    return types;
  }

  private static class Types {
    private final Class<? extends GeneratedMessageV3> messageType;
    private final Class<? extends MessageOrBuilder> messageOrBuilderType;
    private final Class<? extends Node> nodeType;

    public Types(Class<? extends GeneratedMessageV3> messageType, Class<? extends MessageOrBuilder> messageOrBuilderType, Class<? extends Node> nodeType) {
      this.messageType = messageType;
      this.messageOrBuilderType = messageOrBuilderType;
      this.nodeType = nodeType;
    }
  }
}
