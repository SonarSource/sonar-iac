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
package org.sonar.iac.helm.tree;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Collections;
import java.util.List;
import org.sonar.iac.helm.ListNodeOrBuilder;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstUtils.unpack;

public class ListNode implements Node {
  private final long position;
  private final List<Node> nodes;

  public ListNode(long position, List<Node> nodes) {
    this.position = position;
    this.nodes = Collections.unmodifiableList(nodes);
  }

  public static ListNode fromPb(ListNodeOrBuilder nodePb) throws InvalidProtocolBufferException {
    return new ListNode(nodePb.getPos(), unpack(nodePb.getNodesList()));
  }

  @Override
  public NodeType getType() {
    return NodeType.NODE_LIST;
  }

  @Override
  public long getPosition() {
    return position;
  }

  public List<Node> getNodes() {
    return nodes;
  }
}
