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
import org.sonar.iac.helm.protobuf.ListNodeOrBuilder;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.api.Node;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.unpack;

public class ListNodeImpl extends AbstractNode implements ListNode {
  private final List<Node> nodes;

  public ListNodeImpl(long position, List<Node> nodes) {
    super(position);
    this.nodes = Collections.unmodifiableList(nodes);
  }

  public static Node fromPb(ListNodeOrBuilder nodePb) {
    return new ListNodeImpl(nodePb.getPos(), unpack(nodePb.getNodesList()));
  }

  public List<Node> nodes() {
    return nodes;
  }
}
