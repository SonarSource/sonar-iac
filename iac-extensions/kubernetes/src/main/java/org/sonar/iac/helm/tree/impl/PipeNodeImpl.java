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

import org.sonar.iac.helm.protobuf.PipeNodeOrBuilder;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;
import org.sonar.iac.helm.tree.api.VariableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipeNodeImpl extends AbstractNode implements PipeNode {
  private final List<VariableNode> declarations;
  private final List<CommandNode> commands;

  public PipeNodeImpl(long position, long length, List<VariableNode> declarations, List<CommandNode> commands) {
    super(position, length);
    this.declarations = Collections.unmodifiableList(declarations);
    this.commands = Collections.unmodifiableList(commands);
  }

  public static Node fromPb(PipeNodeOrBuilder nodePb) {
    return new PipeNodeImpl(
      nodePb.getPos(),
      nodePb.getLength(),
      nodePb.getDeclList().stream().map(node -> (VariableNode) VariableNodeImpl.fromPb(node)).toList(),
      nodePb.getCmdsList().stream().map(node -> (CommandNode) CommandNodeImpl.fromPb(node)).toList());
  }

  public List<VariableNode> declarations() {
    return declarations;
  }

  public List<CommandNode> commands() {
    return commands;
  }

  @Override
  public List<Node> children() {
    List<Node> children = new ArrayList<>();
    children.addAll(declarations);
    children.addAll(commands);
    return children;
  }
}
