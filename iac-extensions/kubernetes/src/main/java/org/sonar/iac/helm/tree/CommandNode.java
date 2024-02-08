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
import org.sonar.iac.helm.CommandNodeOrBuilder;
import org.sonar.iac.helm.tree.utils.GoTemplateAstUtils;

public class CommandNode implements Node {
  private final long position;
  private final List<Node> arguments;

  public CommandNode(long position, List<Node> arguments) {
    this.position = position;
    this.arguments = Collections.unmodifiableList(arguments);
  }

  public static Node fromPb(CommandNodeOrBuilder nodePb) {
    return new CommandNode(nodePb.getPos(), GoTemplateAstUtils.unpack(nodePb.getArgsList()));
  }

  @Override
  public NodeType getType() {
    return NodeType.NODE_COMMAND;
  }

  @Override
  public long getPosition() {
    return position;
  }

  public List<Node> getArguments() {
    return arguments;
  }
}
