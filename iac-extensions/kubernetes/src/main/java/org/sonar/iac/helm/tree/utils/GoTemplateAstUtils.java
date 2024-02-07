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
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.iac.helm.ListNode;
import org.sonar.iac.helm.NumberNode;
import org.sonar.iac.helm.StringNode;
import org.sonar.iac.helm.tree.Node;

public class GoTemplateAstUtils {
  private GoTemplateAstUtils() {
    // utility class
  }

  public static Node unpackNode(Any nodePb) throws InvalidProtocolBufferException {
    var className = nodePb.getTypeUrl().substring(nodePb.getTypeUrl().lastIndexOf('/') + 1);
    AnyToNodeConverter converter;
    switch (className) {
//      case "org.sonar.iac.helm.ActionNode":
//        converter = any -> org.sonar.iac.helm.tree.ActionNode.fromPb(toTypedNode(org.sonar.iac.helm.ActionNode.class, any));
//        break;
//      case "org.sonar.iac.helm.BoolNode":
//        converter = any -> org.sonar.iac.helm.tree.BoolNode.fromPb(toTypedNode(org.sonar.iac.helm.BoolNode.class, any));
//        break;
//      case "org.sonar.iac.helm.BreakNode":
//        converter = any -> org.sonar.iac.helm.tree.BreakNode.fromPb(toTypedNode(org.sonar.iac.helm.BreakNode.class, any));
//        break;
//      case "org.sonar.iac.helm.ChainNode":
//        converter = any -> org.sonar.iac.helm.tree.ChainNode.fromPb(toTypedNode(org.sonar.iac.helm.ChainNode.class, any));
//        break;
//      case "org.sonar.iac.helm.CommandNode":
//        converter = any -> org.sonar.iac.helm.tree.CommandNode.fromPb(toTypedNode(org.sonar.iac.helm.CommandNode.class, any));
//        break;
//      case "org.sonar.iac.helm.CommentNode":
//        converter = any -> org.sonar.iac.helm.tree.CommentNode.fromPb(toTypedNode(org.sonar.iac.helm.CommentNode.class, any));
//        break;
//      case "org.sonar.iac.helm.ContinueNode":
//        converter = any -> org.sonar.iac.helm.tree.ContinueNode.fromPb(toTypedNode(org.sonar.iac.helm.ContinueNode.class, any));
//        break;
//      case "org.sonar.iac.helm.DotNode":
//        converter = any -> org.sonar.iac.helm.tree.DotNode.fromPb(toTypedNode(org.sonar.iac.helm.DotNode.class, any));
//        break;
//      case "org.sonar.iac.helm.FieldNode":
//        converter = any -> org.sonar.iac.helm.tree.FieldNode.fromPb(toTypedNode(org.sonar.iac.helm.FieldNode.class, any));
//        break;
//      case "org.sonar.iac.helm.IdentifierNode":
//        converter = any -> org.sonar.iac.helm.tree.IdentifierNode.fromPb(toTypedNode(org.sonar.iac.helm.IdentifierNode.class, any));
//        break;
//      case "org.sonar.iac.helm.IfNode":
//        converter = any -> org.sonar.iac.helm.tree.IfNode.fromPb(toTypedNode(org.sonar.iac.helm.IfNode.class, any));
//        break;
      case "org.sonar.iac.helm.ListNode":
        converter = any -> org.sonar.iac.helm.tree.ListNode.fromPb(toTypedNode(ListNode.class, any));
        break;
//      case "org.sonar.iac.helm.NilNode":
//        converter = any -> org.sonar.iac.helm.tree.NilNode.fromPb(toTypedNode(org.sonar.iac.helm.NilNode.class, any));
//        break;
      case "org.sonar.iac.helm.NumberNode":
        converter = any -> org.sonar.iac.helm.tree.NumberNode.fromPb(toTypedNode(NumberNode.class, nodePb));
        break;
//      case "org.sonar.iac.helm.PipeNode":
//        converter = any -> org.sonar.iac.helm.tree.PipeNode.fromPb(toTypedNode(org.sonar.iac.helm.PipeNode.class, any));
//        break;
//      case "org.sonar.iac.helm.RangeNode":
//        converter = any -> org.sonar.iac.helm.tree.RangeNode.fromPb(toTypedNode(org.sonar.iac.helm.RangeNode.class, any));
//        break;
      case "org.sonar.iac.helm.StringNode":
        converter = any -> org.sonar.iac.helm.tree.StringNode.fromPb(toTypedNode(StringNode.class, nodePb));
        break;
//      case "org.sonar.iac.helm.TemplateNode":
//        converter = any -> org.sonar.iac.helm.tree.TemplateNode.fromPb(toTypedNode(org.sonar.iac.helm.TemplateNode.class, any));
//        break;
//      case "org.sonar.iac.helm.TextNode":
//        converter = any -> org.sonar.iac.helm.tree.TextNode.fromPb(toTypedNode(org.sonar.iac.helm.TextNode.class, any));
//        break;
//      case "org.sonar.iac.helm.VariableNode":
//        converter = any -> org.sonar.iac.helm.tree.VariableNode.fromPb(toTypedNode(org.sonar.iac.helm.VariableNode.class, any));
//        break;
//      case "org.sonar.iac.helm.WithNode":
//        converter = any -> org.sonar.iac.helm.tree.WithNode.fromPb(toTypedNode(org.sonar.iac.helm.WithNode.class, any));
//        break;
      default:
        return null;
    }
    return converter.convert(nodePb);
  }

  public static List<Node> unpack(Collection<Any> nodesPb) {
    return nodesPb.stream()
      .map((Any nodePb) -> {
        try {
          return unpackNode(nodePb);
        } catch (InvalidProtocolBufferException e) {
          throw new IllegalStateException("Failed to unpack node", e);
        }
      })
      .collect(Collectors.toList());
  }

  private static <T extends Message> T toTypedNode(Class<T> nodeType, Any nodePb) throws InvalidProtocolBufferException {
    if (nodePb.is(nodeType)) {
      return nodePb.unpack(nodeType);
    }
    throw new IllegalArgumentException("Expected " + nodeType + " but got " + nodePb.getTypeUrl());
  }
}
