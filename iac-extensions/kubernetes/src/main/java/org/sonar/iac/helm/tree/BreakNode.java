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

public class BreakNode extends AbstractNode {
  private final long line;

  public BreakNode(long position, long line) {
    super(position);
    this.line = line;
  }

  public static Node fromPb(org.sonar.iac.helm.BreakNodeOrBuilder breakNodePb) {
    return new BreakNode(breakNodePb.getPos(), breakNodePb.getLine());
  }

  @Override
  public NodeType type() {
    return NodeType.NODE_BREAK;
  }

  public long getLine() {
    return line;
  }
}
