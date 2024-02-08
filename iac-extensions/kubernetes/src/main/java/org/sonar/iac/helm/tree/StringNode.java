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

public class StringNode implements Node {
  private final long position;
  private final String text;

  public StringNode(long position, String text) {
    this.position = position;
    this.text = text;
  }

  public static Node fromPb(org.sonar.iac.helm.StringNode nodePb) {
    return new StringNode(nodePb.getPos(), nodePb.getText());
  }

  @Override
  public NodeType getType() {
    return NodeType.NODE_STRING;
  }

  @Override
  public long getPosition() {
    return position;
  }

  public String getText() {
    return text;
  }
}
