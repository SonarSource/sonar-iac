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

import org.sonar.iac.helm.RangeNodeOrBuilder;

public class RangeNode extends AbstractBranchNode {
  public RangeNode(long position, PipeNode pipe, ListNode list, ListNode elseList) {
    super(position, pipe, list, elseList);
  }

  public static Node fromPb(RangeNodeOrBuilder nodePb) {
    return new RangeNode(
      nodePb.getPos(),
      (PipeNode) PipeNode.fromPb(nodePb.getBranchNode().getPipe()),
      (ListNode) ListNode.fromPb(nodePb.getBranchNode().getList()),
      (ListNode) ListNode.fromPb(nodePb.getBranchNode().getElseList()));
  }

  @Override
  public NodeType type() {
    return NodeType.NODE_RANGE;
  }
}
