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
import com.google.protobuf.Message;
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

public final class GoTemplateAstUtils {
  private static final Logger LOG = LoggerFactory.getLogger(GoTemplateAstUtils.class);
  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  private GoTemplateAstUtils() {
    // utility class
  }

  @CheckForNull
  public static Node unpackNode(Any nodePb) {
    try {
      Class<GeneratedMessageV3> messageClass = messageType(nodePb);
      Class<MessageOrBuilder> messageClassOrBuilder = messageTypeOrBuilder(nodePb);
      Class<Node> targetClass = treeTypeToMessageType(nodePb);
      var handle = lookup.findStatic(targetClass, "fromPb", MethodType.methodType(Node.class, messageClassOrBuilder));
      return (Node) handle.invoke(nodePb.unpack(messageClass));
    } catch (Throwable t) {
      LOG.debug("Failed to unpack node", t);
      return null;
    }
  }

  public static List<Node> unpack(Collection<Any> nodesPb) {
    return nodesPb.stream()
      .map(GoTemplateAstUtils::unpackNode)
      .collect(Collectors.toList());
  }

  private static <T extends Message> Class<T> messageType(Any nodePb) throws ClassNotFoundException {
    return (Class<T>) Class.forName(typeName(nodePb));
  }

  private static <T extends MessageOrBuilder> Class<T> messageTypeOrBuilder(Any nodePb) throws ClassNotFoundException {
    return (Class<T>) Class.forName(typeName(nodePb) + "OrBuilder");
  }

  private static <T extends Node> Class<T> treeTypeToMessageType(AnyOrBuilder anyPb) throws ClassNotFoundException {
    var typeName = typeName(anyPb);
    var packageName = typeName.substring(0, typeName.lastIndexOf("."));
    var className = typeName.substring(typeName.lastIndexOf(".") + 1);
    return (Class<T>) Class.forName(packageName + ".tree." + className);
  }

  private static String typeName(AnyOrBuilder nodePb) {
    return nodePb.getTypeUrl().substring(nodePb.getTypeUrl().lastIndexOf('/') + 1);
  }
}
