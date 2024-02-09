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

import org.sonar.iac.helm.ActionNodeOrBuilder;

public class ActionNode extends AbstractNode {
  private final PipeNode pipe;

  public ActionNode(long position, PipeNode pipe) {
    super(position);
    this.pipe = pipe;
  }

  public static Node fromPb(ActionNodeOrBuilder nodePb) {
    return new ActionNode(nodePb.getPos(), (PipeNode) PipeNode.fromPb(nodePb.getPipe()));
  }

  @Override
  public NodeType type() {
    return NodeType.NODE_ACTION;
  }

  public PipeNode pipe() {
    return pipe;
  }
}
