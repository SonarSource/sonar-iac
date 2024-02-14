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

import java.util.Collections;
import java.util.List;
import org.sonar.iac.helm.protobuf.VariableNodeOrBuilder;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.VariableNode;

public class VariableNodeImpl extends AbstractNode implements VariableNode {
  private final List<String> ident;

  public VariableNodeImpl(long position, long length, List<String> ident) {
    super(position, length);
    this.ident = Collections.unmodifiableList(ident);
  }

  public static Node fromPb(VariableNodeOrBuilder nodePb) {
    return new VariableNodeImpl(nodePb.getPos(), nodePb.getLength(), nodePb.getIdentList());
  }

  public List<String> idents() {
    return ident;
  }
}
