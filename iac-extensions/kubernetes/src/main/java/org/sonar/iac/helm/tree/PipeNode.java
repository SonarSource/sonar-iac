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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.iac.helm.PipeNodeOrBuilder;

public class PipeNode extends AbstractNode {
  private final List<VariableNode> declarations;
  private final List<CommandNode> commands;

  public PipeNode(long position, List<VariableNode> declarations, List<CommandNode> commands) {
    super(position);
    this.declarations = Collections.unmodifiableList(declarations);
    this.commands = Collections.unmodifiableList(commands);
  }

  public static Node fromPb(PipeNodeOrBuilder nodePb) {
    return new PipeNode(nodePb.getPos(),
      nodePb.getDeclList().stream().map(node -> (VariableNode) VariableNode.fromPb(node)).collect(Collectors.toList()),
      nodePb.getCmdsList().stream().map(node -> (CommandNode) CommandNode.fromPb(node)).collect(Collectors.toList()));
  }

  @Override
  public NodeType type() {
    return NodeType.NODE_PIPE;
  }

  public List<VariableNode> declarations() {
    return declarations;
  }

  public List<CommandNode> commands() {
    return commands;
  }
}
