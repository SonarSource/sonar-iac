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
package org.sonar.iac.helm.tree.impl;

import org.sonar.iac.helm.protobuf.IfNodeOrBuilder;
import org.sonar.iac.helm.tree.api.IfNode;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;

public class IfNodeImpl extends AbstractBranchNode implements IfNode {
  public IfNodeImpl(long position, long length, PipeNode pipe, ListNode list, ListNode elseList) {
    super(position, length, pipe, list, elseList);
  }

  public static Node fromPb(IfNodeOrBuilder ifNodePb) {
    var pipe = (PipeNode) PipeNodeImpl.fromPb(ifNodePb.getBranchNode().getPipe());
    var list = (ListNode) ListNodeImpl.fromPb(ifNodePb.getBranchNode().getList());
    var elseList = (ListNode) ListNodeImpl.fromPb(ifNodePb.getBranchNode().getElseList());
    return new IfNodeImpl(ifNodePb.getPos(), ifNodePb.getLength(), pipe, list, elseList);
  }
}
